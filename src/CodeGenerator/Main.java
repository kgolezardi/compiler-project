package CodeGenerator;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        CodeGenerator codeGenerator = new CodeGenerator("runtime/code.txt");
        codeGenerator.parse();
    }
}
