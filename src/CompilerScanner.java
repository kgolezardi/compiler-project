import java.io.*;
import java.util.*;
import java.util.regex.*;

public class CompilerScanner {
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

    private static int get_next_state(int state, char c) {
        for (int i = 0; i < dfa.get(state).size(); i++)
        {
            Pattern opPatternRegex = Pattern.compile(dfa.get(state).get(i).pattern);
            Matcher matcher = opPatternRegex.matcher(new StringBuilder(1).append(c));

            if (matcher.find())
                return dfa.get(state).get(i).state;
        }

        throw new IllegalArgumentException();
    }

    private static Token get_next_token(String text, Integer pointer) {
        int state = 0, previousState = -1;
        StringBuilder tokenText = new StringBuilder();
        try {
            while (states.get(state) != StateType.FINISH) {
                previousState = state;
                state = get_next_state(state, text.charAt(pointer));
                tokenText.append(text.charAt(pointer++));

                if (pointer == text.length())
                    break;
            }
        } catch (Exception IllegalArgumentException) {
            tokenText.append(text.charAt(pointer));
            return new Token(Token.TokenType.ERROR, tokenText.toString());
        }

        // TODO: handle if privous state is reject (for example /dsf)
        Token.TokenType tokenType;
        if (states.get(state) == StateType.FINISH) {
            tokenText.deleteCharAt(tokenText.length() - 1);
            tokenType = Token.TokenType.valueOf(states.get(previousState).toString());
        } else {
            tokenType = Token.TokenType.valueOf(states.get(state).toString());
        }

        if (keywords.contains(tokenText.toString()))
            tokenType = Token.TokenType.KEYWORD;
        return new Token(tokenType, tokenText.toString());
    }

    public static void main(String[] args) throws IOException {
        initialize();

        String text = new Scanner(new File("code.txt")).useDelimiter("\\Z").next();

        FileWriter tokens_file = new FileWriter("scanner.txt");

        int pointer = 0, previousLine = 0, number_of_tokens_of_line = 0;
        while (pointer < text.length()) {
            // TODO: print line number (Golezardi)
            // TODO: print errors (Golezardi)

            if (text.charAt(pointer) == '\n')
                lineNumber++;

            Token token = get_next_token(text, pointer);
            pointer += token.text.length();
            if (token.type != Token.TokenType.WHITESPACE
                    && token.type != Token.TokenType.COMMENT
                    && token.type != Token.TokenType.ERROR)
            {
                if (previousLine != lineNumber && number_of_tokens_of_line == 0)
                {
                    previousLine = lineNumber;
                    tokens_file.write(lineNumber + ". ");
                }

                number_of_tokens_of_line++;

                tokens_file.write("(" + token.type + ", " + token.text + ")");
            }

            if (previousLine != lineNumber && number_of_tokens_of_line > 0)
            {
                number_of_tokens_of_line = 0;
                tokens_file.write("\n");
            }

        }

        tokens_file.close();
    }
}
