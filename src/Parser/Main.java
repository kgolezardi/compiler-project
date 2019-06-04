package Parser;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser("runtime/code.txt");
        parser.parse();

        FileWriter errorsFile = new FileWriter("runtime/errors.txt");
        for (String error : parser.getErrors())
            errorsFile.write(error + "\n");
        errorsFile.close();
    }
}
