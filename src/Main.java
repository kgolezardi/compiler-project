import CodeGenerator.CodeGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        CodeGenerator codeGenerator = new CodeGenerator("runtime/code.txt");
        List<String> code = codeGenerator.getCode();

        FileWriter errorFileWriter = new FileWriter("runtime/errors.txt");
        for (String error : codeGenerator.getErrors())
            errorFileWriter.write(error + "\n");
        errorFileWriter.close();

        FileWriter outputFileWriter = new FileWriter("runtime/output.txt");
        for (int i = 0; i < code.size(); i++)
            outputFileWriter.write(String.format("%d\t%s\n", i, code.get(i)));
        outputFileWriter.close();
    }
}
