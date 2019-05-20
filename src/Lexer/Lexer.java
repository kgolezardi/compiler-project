package Lexer;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Lexer {
    private enum StateType {NUM, ID, SYMBOL, COMMENT, WHITESPACE, REJECT, START, FINISH}

    private static Map<Integer, List<DfaEdge>> dfa;
    private static Map<Integer, StateType> states;
    private static Set<String> keywords;
    private FileInputStream fis;
    private char current;

    static {
        try {
            initialize();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void initialize() throws FileNotFoundException {
        dfa = new HashMap<>();
        states = new HashMap<>();

        Scanner dfaScanner = new Scanner(new File("dfa.txt"));
        while (dfaScanner.hasNext()) {
            int state = dfaScanner.nextInt();
            String type = dfaScanner.next();
            states.put(state, StateType.valueOf(type));

            List<DfaEdge> edges = new ArrayList<>();
            int edgeCount = dfaScanner.nextInt();
            for (int i = 0; i < edgeCount; i++) {
                String pattern = dfaScanner.next();
                int nextState = dfaScanner.nextInt();
                edges.add(new DfaEdge(pattern, nextState));
            }

            dfa.put(state, edges);
        }

        keywords = new HashSet<>();
        Scanner keywordScanner = new Scanner(new File("keywords.txt"));
        while (keywordScanner.hasNext()) {
            keywords.add(keywordScanner.next());
        }
    }

    public Lexer(String filename) throws IOException {
        fis = new FileInputStream(new File(filename));
        current = (char) fis.read();
    }

    private static int getNextState(int state, char c) {
        for (DfaEdge edge : dfa.get(state)) {
            Pattern opPatternRegex = Pattern.compile(edge.pattern);
            Matcher matcher = opPatternRegex.matcher(new StringBuilder(1).append(c));

            if (matcher.find())
                return edge.state;
        }

        throw new IllegalArgumentException();
    }

    public boolean hasNextToken() {
        return current != 0;
    }

    public Token getNextToken() throws IOException {
        int state = 0, previousState = -1;
        StringBuilder tokenText = new StringBuilder();
        try {
            while (states.get(state) != StateType.FINISH) {
                if (state != 0) {
                    previousState = state;
                    tokenText.append(current);

                    if (fis.available() <= 0) {
                        current = 0;
                        break;
                    }
                    current = (char) fis.read();
                }
                state = getNextState(state, current);
            }
        } catch (IllegalArgumentException e) {
            if (previousState == -1 || states.get(previousState) != StateType.REJECT) {
                tokenText.append(current);
                current = (char) fis.read();
            }
            return new Token(Token.TokenType.ERROR, tokenText.toString());
        }

        StateType stateType = states.get(previousState);
        if (stateType == StateType.REJECT)
            return new Token(Token.TokenType.ERROR, tokenText.toString());

        Token.TokenType tokenType = Token.TokenType.valueOf(stateType.toString());
        if (keywords.contains(tokenText.toString()))
            tokenType = Token.TokenType.KEYWORD;
        return new Token(tokenType, tokenText.toString());
    }
}
