package CodeGenerator;

class SymbolTableEntry {
    public enum TypeSpecifier {INT, VOID}

    int address;
    String lexeme;
    TypeSpecifier type;


    SymbolTableEntry(int address, String lexeme, TypeSpecifier type) {
        this.address = address;
        this.lexeme = lexeme;
        this.type = type;
    }
}
