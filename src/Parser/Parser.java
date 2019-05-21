package Parser;

import Lexer.Lexer;
import Lexer.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Parser {
    private static Map<String, Diagram> diagrams;
    private static Map<String, List<String>> first;
    private static Map<String, List<String>> follow;
    private Lexer lexer;
    private Token currentToken;

    static {
        try {
            initializeDiagram();

            first = new HashMap<>();
            follow = new HashMap<>();

            // First and follow sets are obtained from http://smlweb.cpsc.ucalgary.ca
            initializeSet(first, "first.txt");
            initializeSet(follow, "follow.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDiagram() throws FileNotFoundException {
        diagrams = new HashMap<>();

        Scanner grammarScanner = new Scanner(new File("grammar.txt"));
        while (grammarScanner.hasNext()) {
            String nonTerminal = grammarScanner.next();
            grammarScanner.next();

            Diagram diagram = new Diagram(nonTerminal);
            diagrams.put(nonTerminal, diagram);

            boolean hasNext = true;
            while (hasNext)
                hasNext = diagram.createPath(grammarScanner);
        }
    }

    private static void initializeSet(Map<String, List<String>> set, String pathname) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(pathname));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = line.split(" ");
            List<String> setValues = new ArrayList<>();

            for (int i = 1; i < values.length; i++)
                if (values[i].equals("."))
                    setValues.add("");
                else
                    setValues.add(values[i]);

            set.put(values[0], setValues);
        }
    }

    public Parser(String filename) throws IOException {
        lexer = new Lexer(filename);
        currentToken = lexer.getNextToken();
    }

    private String rawToken() {
        switch (currentToken.type) {
            case KEYWORD:
                return currentToken.text;
            case NUM:
                return "num";
            case ID:
                return "id";
            case SYMBOL:
                return currentToken.text;
            case EOF:
                return "$";
        }
        return "";
    }

    public DiagramNode getNextNode(Diagram diagram, DiagramNode node, ParseTreeNode parseTreeNode) throws IOException {
        for (DiagramEdge edge : node.edges) {
            if (edge.isTerminal() && edge.label.equals(rawToken())) {
                do {
                    currentToken = lexer.getNextToken();
                    // TODO: write lexical error in file
                } while (currentToken.type == Token.TokenType.ERROR ||
                        currentToken.type == Token.TokenType.WHITESPACE ||
                        currentToken.type == Token.TokenType.COMMENT);
                ParseTreeNode child = new ParseTreeNode(edge.label);
                parseTreeNode.children.add(child);
                return edge.nextNode;
            }
            if (edge.label.equals("")) {
                if (exists(rawToken(), follow.get(diagram.nonTerminal)))
                        return edge.nextNode;
            } else if (!edge.isTerminal()) {
                if (exists(rawToken(), first.get(edge.label))) {
                    traverse(diagrams.get(edge.label), parseTreeNode);
                    return edge.nextNode;
                }
            }
        }

        for (DiagramEdge edge : node.edges)
            if (!edge.label.equals("") && !edge.isTerminal() &&
                    exists("", first.get(edge.label)) && exists(rawToken(), follow.get(edge.label))) {
                traverse(diagrams.get(edge.label), parseTreeNode);
                return edge.nextNode;
            }

        // TODO: parser error
        System.out.printf("Parser Error while traversing %s trying to match '%s'\n",
                diagram.nonTerminal, currentToken.text);
        System.out.print("The edges are: ");
        for (DiagramEdge edge : node.edges)
            System.out.print(edge.label + " ");
        System.out.println();
        return null;
    }

    private static boolean exists(String str, List<String> list) {
        for (String element : list)
            if (str.equals(element))
                return true;
        return false;
    }

    private ParseTreeNode traverse(Diagram diagram, ParseTreeNode parent) throws IOException {
        ParseTreeNode parseTreeNode = new ParseTreeNode(diagram.nonTerminal);
        if (parent != null)
            parent.children.add(parseTreeNode);

        DiagramNode node = diagram.start;
        while (node != diagram.finish) {
            node = getNextNode(diagram, node, parseTreeNode);
        }

        return parseTreeNode;
    }

    public ParseTreeNode parse() throws IOException {
        return traverse(diagrams.get("PROGRAM"), null);
    }
}
