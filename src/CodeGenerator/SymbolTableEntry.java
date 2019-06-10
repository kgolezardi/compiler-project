package CodeGenerator;

class SymbolTableEntry {
    public enum TypeSpecifier {INT, VOID, ARRAY}

    int address;
    String lexeme;
    TypeSpecifier type;
    SymbolTableAttribute attribute;


    SymbolTableEntry(int address, String lexeme, TypeSpecifier type) {
        this.address = address;
        this.lexeme = lexeme;
        this.type = type;
        this.attribute = null;
    }

    SymbolTableEntry(int address, String lexeme, TypeSpecifier type, SymbolTableAttribute attribute) {
        this(address, lexeme, type);
        this.attribute = attribute;
    }
}
