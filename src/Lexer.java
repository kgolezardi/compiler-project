import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Lexer {
    private enum StateType {NUM, ID, SYMBOL, COMMENT, WHITESPACE, REJECT, START, FINISH}

    private static Map<Integer, List<DfaEdge>> dfa;
    private static Map<Integer, StateType> states;
    private static Set<String> keywords;
    private static int lineNumber = 1;

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

    private static int getNextState(int state, char c) {
        for (DfaEdge edge : dfa.get(state)) {
            Pattern opPatternRegex = Pattern.compile(edge.pattern);
            Matcher matcher = opPatternRegex.matcher(new StringBuilder(1).append(c));

            if (matcher.find())
                return edge.state;
        }

        throw new IllegalArgumentException();
    }

    private static Token getNextToken(String text, Integer pointer) {
        int state = 0, previousState = -1;
        StringBuilder tokenText = new StringBuilder();
        try {
            while (states.get(state) != StateType.FINISH) {
                if (state != 0) {
                    previousState = state;
                    tokenText.append(text.charAt(pointer));
                    pointer++;

                    if (pointer == text.length())
                        break;
                }
                state = getNextState(state, text.charAt(pointer));
            }
        } catch (IllegalArgumentException e) {
            if (previousState == -1 || states.get(previousState) != StateType.REJECT)
                tokenText.append(text.charAt(pointer));
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

    public static void main(String[] args) throws IOException {
        initialize();

        String text = new Scanner(new File("code.txt")).useDelimiter("\\Z").next();

        FileWriter tokensFile = new FileWriter("scanner.txt");
        FileWriter lexicalErrorsFile = new FileWriter("lexical_errors.txt");

        int pointer = 0, lastTokenLine = 0, lastErrorLine = 0;
        while (pointer < text.length()) {
            Token token = getNextToken(text, pointer);
            System.out.println("'" + token.text + "'");
            pointer += token.text.length();
            for (char c : token.text.toCharArray())
                if (c == '\n')
                    lineNumber++;

            if (token.type == Token.TokenType.ERROR) {
                if (lastErrorLine != lineNumber) {
                    if (lastErrorLine != 0)
                        lexicalErrorsFile.write("\n");
                    lastErrorLine = lineNumber;
                    lexicalErrorsFile.write(lineNumber + ". ");
                }
                lexicalErrorsFile.write("(" + token.text + ", " + "invalid input) ");
            }
            else if (token.type != Token.TokenType.WHITESPACE && token.type != Token.TokenType.COMMENT) {
                if (lastTokenLine != lineNumber) {
                    if (lastTokenLine != 0)
                        tokensFile.write("\n");
                    lastTokenLine = lineNumber;
                    tokensFile.write(lineNumber + ". ");
                }
                tokensFile.write("(" + token.type + ", " + token.text + ") ");
            }
        }

        tokensFile.close();
        lexicalErrorsFile.close();
    }
}
