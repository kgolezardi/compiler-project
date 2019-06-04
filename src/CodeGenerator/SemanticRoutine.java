package CodeGenerator;

import Lexer.Token;

public class SemanticRoutine {
    public static void call(String routineName, Token token) {
        switch (routineName) {
            case "#a":
                System.out.println("a");
                break;
        }
    }
}
