package Lexer;

public class Token {
    public enum TokenType {NUM, ID, KEYWORD, SYMBOL, COMMENT, WHITESPACE, EOF, ERROR}

    public TokenType type;
    public String text;

    Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }
}
