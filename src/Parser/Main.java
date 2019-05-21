package Parser;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.printf("%-25s : %25s%n\n", "left justified", "right justified");
        System.out.printf("%25s : %-25s%n\n", "right justified", "left justified");

        Parser parser = new Parser("code.txt");
        ParseTreeNode parseTree = parser.parse();
//        ParseTreeNode a = new ParseTreeNode("A");
//        ParseTreeNode b = new ParseTreeNode("B");
//        ParseTreeNode c = new ParseTreeNode("C");
//        ParseTreeNode d = new ParseTreeNode("D");
//        ParseTreeNode e = new ParseTreeNode("E");
//        ParseTreeNode f = new ParseTreeNode("F");
//        ParseTreeNode g = new ParseTreeNode("G");
//        ParseTreeNode h = new ParseTreeNode("H");
//        ParseTreeNode i = new ParseTreeNode("I");
//        ParseTreeNode j = new ParseTreeNode("J");
//        ParseTreeNode k = new ParseTreeNode("K");
//
//        a.children.add(b);
//        a.children.add(c);
//        b.children.add(d);
//        d.children.add(g);
//        d.children.add(h);
//        c.children.add(e);
//        c.children.add(f);
//        f.children.add(i);
//        f.children.add(j);
//        f.children.add(k);
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
