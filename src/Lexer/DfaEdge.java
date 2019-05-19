package Lexer;

public class DfaEdge {
    public String pattern;
    public Integer state;

    public DfaEdge(String pattern, Integer state) {
        this.pattern = pattern;
        this.state = state;
    }
}
