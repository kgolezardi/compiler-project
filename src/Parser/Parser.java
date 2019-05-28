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
    private List<String> errors;
    private boolean finished = false;

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
        errors = new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
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
            if (edge.isTerminal() && edge.label.equals(rawToken()))
                return matchTerminalEdge(parseTreeNode, edge);

            if (edge.label.equals("")) {
                if (exists(rawToken(), follow.get(diagram.nonTerminal)))
                        return edge.nextNode;
            } else if (!edge.isTerminal()) {
                if (exists(rawToken(), first.get(edge.label)))
                    return matchNonTerminalEdge(parseTreeNode, edge);
            }
        }

        for (DiagramEdge edge : node.edges)
            if (!edge.label.equals("") && !edge.isTerminal() &&
                    exists("", first.get(edge.label)) && exists(rawToken(), follow.get(edge.label)))
                return matchNonTerminalEdge(parseTreeNode, edge);

        // Error Handling
        DiagramEdge edge = node.edges.get(0);
        if (edge.isTerminal()) {
            if (edge.label.equals("$")) {
                errors.add(String.format("%d: Syntax Error! Malformed Input", lexer.getLineNumber()));
                setFinish();
                return null;
            }

            errors.add(String.format("%d: Syntax Error! Missing '%s'", lexer.getLineNumber(), edge.label));
            return matchTerminalEdge(parseTreeNode, edge);
        } else {
            while (!exists(rawToken(), first.get(edge.label)) && !exists(rawToken(), follow.get(edge.label))) {
                errors.add(String.format("%d: Syntax Error! Unexpected '%s' instead of '%s'", lexer.getLineNumber(),
                        currentToken.text, edge.label));
                updateCurrentToken();
                if (currentToken.type == Token.TokenType.EOF) {
                    errors.add(String.format("%d: Syntax Error! Unexpected EndOfFile", lexer.getLineNumber()));
                    setFinish();
                    return null;
                }
            }
            if (exists(rawToken(), first.get(edge.label)) ||
                    exists("", first.get(edge.label)) && exists(rawToken(), follow.get(edge.label)))
                return matchNonTerminalEdge(parseTreeNode, edge);

            errors.add(String.format("%d: Syntax Error! Missing '%s' like '%s'", lexer.getLineNumber(), edge.label,
                    nonTerminalDescription(edge.label)));

            ParseTreeNode child = new ParseTreeNode(edge.label);
            parseTreeNode.children.add(child);
            return edge.nextNode;
        }
    }

    private void setFinish() {
        finished = true;
    }

    private String nonTerminalDescription(String nonTerminal) {
        StringBuilder description = new StringBuilder();
        Diagram diagram = diagrams.get(nonTerminal);
        DiagramNode node = diagram.start;
        while (node != diagram.finish) {
            DiagramEdge edge = node.edges.get(node.edges.size() - 1);
            if (edge.isTerminal() || edge.label.equals(""))
                description.append(edge.label);
            else
                description.append(nonTerminalDescription(edge.label));
            node = edge.nextNode;
        }
        return description.toString();
    }

    private DiagramNode matchTerminalEdge(ParseTreeNode parseTreeNode, DiagramEdge edge) throws IOException {
        updateCurrentToken();
        ParseTreeNode child = new ParseTreeNode(edge.label);
        parseTreeNode.children.add(child);
        return edge.nextNode;
    }

    private DiagramNode matchNonTerminalEdge(ParseTreeNode parseTreeNode, DiagramEdge edge) throws IOException {
        traverse(diagrams.get(edge.label), parseTreeNode);
        return edge.nextNode;
    }

    private void updateCurrentToken() throws IOException {
        do {
            currentToken = lexer.getNextToken();
            if (currentToken.type == Token.TokenType.ERROR)
                errors.add(String.format("%d: Syntax Error! Invalid input '%s'", lexer.getLineNumber(), currentToken.text));
        } while (currentToken.type == Token.TokenType.ERROR ||
                currentToken.type == Token.TokenType.WHITESPACE ||
                currentToken.type == Token.TokenType.COMMENT);
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
        while (node != diagram.finish && !finished) {
            node = getNextNode(diagram, node, parseTreeNode);
        }

        return parseTreeNode;
    }

    public ParseTreeNode parse() throws IOException {
        return traverse(diagrams.get("PROGRAM"), null);
    }
}
