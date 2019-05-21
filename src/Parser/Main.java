package Parser;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser("code.txt");
        ParseTreeNode parseTree = parser.parse();

        FileWriter parseTreeFile = new FileWriter("parse-tree.txt");
        printTree(parseTreeFile, parseTree, 0);
        parseTreeFile.close();
    }

    private static void printTree(FileWriter file, ParseTreeNode node, int depth) throws IOException {
        for (int i = 0; i < depth; i++)
            file.write(String.format("%-10s", "|"));
        String label = node.label;
        if (label.length() > 10)
            label = label.substring(0, 5) + label.substring(label.length() - 5);
        file.write(String.format("%-10s\n", label));
        for (ParseTreeNode child : node.children) {
            printTree(file, child, depth + 1);
        }
    }
}
