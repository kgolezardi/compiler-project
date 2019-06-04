package Lexer;

class DfaEdge {
    String pattern;
    Integer state;

    DfaEdge(String pattern, Integer state) {
        this.pattern = pattern;
        this.state = state;
    }
}
