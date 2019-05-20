package Lexer;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("code.txt");
        int lastTokenLine = 0, lastErrorLine = 0, lineNumber = 1;

        FileWriter tokensFile = new FileWriter("scanner.txt");
        FileWriter lexicalErrorsFile = new FileWriter("lexical_errors.txt");

        while (lexer.hasNextToken()) {
            Token token = lexer.getNextToken();

            for (char c : token.text.toCharArray())
                if (c == '\n')
                    lineNumber++;

            if (token.type == Token.TokenType.ERROR) {
                lastErrorLine = getLastLine(lineNumber, lastErrorLine, lexicalErrorsFile);
                lexicalErrorsFile.write("(" + token.text + ", " + "invalid input) ");
            }
            else if (token.type != Token.TokenType.WHITESPACE && token.type != Token.TokenType.COMMENT) {
                lastTokenLine = getLastLine(lineNumber, lastTokenLine, tokensFile);
                tokensFile.write("(" + token.type + ", " + token.text + ") ");
            }
        }

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
