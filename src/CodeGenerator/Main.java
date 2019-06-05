package CodeGenerator;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        CodeGenerator codeGenerator = new CodeGenerator("runtime/code.txt");
        codeGenerator.getCode();
        for (SymbolTableEntry ste : codeGenerator.symbolTable) {
            System.out.printf("%d %s %s\n", ste.address, ste.lexeme, ste.type);
        }

        for (String command : codeGenerator.programBlock) {
            System.out.printf("%s\n", command);
        }
    }
}
