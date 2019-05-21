package Lexer;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("runtime/code.txt");
        int lastTokenLine = 0, lastErrorLine = 0;

        FileWriter tokensFile = new FileWriter("runtime/scanner.txt");
        FileWriter lexicalErrorsFile = new FileWriter("runtime/lexical_errors.txt");

        Token token;
        do {
            token = lexer.getNextToken();

            if (token.type == Token.TokenType.ERROR) {
                lastErrorLine = getLastLine(lexer.getLineNumber(), lastErrorLine, lexicalErrorsFile);
                lexicalErrorsFile.write("(" + token.text + ", " + "invalid input) ");
            }
            else if (token.type != Token.TokenType.WHITESPACE && token.type != Token.TokenType.COMMENT) {
                lastTokenLine = getLastLine(lexer.getLineNumber(), lastTokenLine, tokensFile);
                tokensFile.write("(" + token.type + ", " + token.text + ") ");
            }
        } while (token.type != Token.TokenType.EOF);

        tokensFile.close();
        lexicalErrorsFile.close();
    }

    private static int getLastLine(int lineNumber, int lastLine, FileWriter file) throws IOException {
        if (lastLine != lineNumber) {
            if (lastLine != 0)
                file.write("\n");
            lastLine = lineNumber;
            file.write(lineNumber + ". ");
        }
        return lastLine;
    }
}
