import java.io.*;
import java.util.*;

public class CompilerScanner {
    private enum StateType {NUM, ID, SYMBOL, COMMENT, WHITESPACE, REJECT, FINISH}

    private static Map<Integer, List<DfaEdge>> dfa;
    private static Map<Integer, StateType> states;
    private static int lineNumber = 0;

    private static void initialize() throws FileNotFoundException {
        dfa = new HashMap<>();

        File file = new File("dfa.txt");
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {
            int state = scanner.nextInt();
            // TODO: input state types (Golezardi)

            List<DfaEdge> edges = new ArrayList<>();
            int edgeCount = scanner.nextInt();
            for (int i = 0; i < edgeCount; i++) {
                String pattern = scanner.next();
                int nextState = scanner.nextInt();
                edges.add(new DfaEdge(pattern, nextState));
            }

            dfa.put(state, edges);
        }

        // TODO: input keywords (Golezardi)
    }

    private static int get_next_state(int state, char c) {
        // TODO: get next state (Gorji)
        // TODO: throw error (Gorji)
        // TODO: update line number (Gorji)
        return 0;
    }

    private static Token get_next_token(String text, Integer pointer) {
        int state = 0, previousState = -1;
        StringBuilder tokenText = new StringBuilder();
        try {
            while (states.get(state) != StateType.FINISH) {
                previousState = state;
                state = get_next_state(state, text.charAt(pointer));
                tokenText.append(text.charAt(pointer++));
            }
        } catch (Exception e) {
            tokenText.append(text.charAt(pointer));
            return new Token(Token.TokenType.ERROR, tokenText.toString());
        }
        tokenText.deleteCharAt(tokenText.length() - 1);
        // TODO: handle keywords (Golezardi)
        return new Token(Token.TokenType.valueOf(states.get(previousState).toString()), tokenText.toString());
    }

    public static void main(String[] args) throws IOException {
        initialize();

        String text = new Scanner(new File("code.txt")).useDelimiter("\\Z").next();
        int pointer = 0, previousLine = -1;
        while (pointer < text.length()) {
            // TODO: print line number (Golezardi)
            // TODO: print errors (Golezardi)
            Token token = get_next_token(text, pointer);
            pointer += token.text.length();
            System.out.printf("(%s, %s) ", token.type, token.text);
        }
    }
}
