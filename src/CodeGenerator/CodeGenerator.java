package CodeGenerator;

import Lexer.Lexer;
import Lexer.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class CodeGenerator {
    private static Map<String, Diagram> diagrams;
    private static Map<String, List<String>> first;
    private static Map<String, List<String>> follow;
    private Lexer lexer;
    Token currentToken;
    private List<String> errors;
    private boolean finished = false;
    Stack<String> semanticStack;
    List<SymbolTableEntry> symbolTable;
    int dataBlockAddress;
    int tempBlockAddress;
    List<String> programBlock;

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

    private static boolean exists(String str, List<String> list) {
        for (String element : list)
            if (str.equals(element))
                return true;
        return false;
    }

    public CodeGenerator(String filename) throws IOException {
        lexer = new Lexer(filename);
        currentToken = lexer.getNextToken();
        errors = new ArrayList<>();
        semanticStack = new Stack<>();
        symbolTable = new ArrayList<>();
        dataBlockAddress = 500;
        tempBlockAddress = 1000;
        programBlock = new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    private void setFinish() {
        finished = true;
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

    private DiagramNode matchTerminalEdge(DiagramEdge edge) throws IOException {
        for (String routine : edge.preRoutines)
            SemanticRoutines.call(routine, this);
        updateCurrentToken();
        for (String routine : edge.postRoutines)
            SemanticRoutines.call(routine, this);
        return edge.nextNode;
    }

    private DiagramNode matchNonTerminalEdge(DiagramEdge edge) throws IOException {
        for (String routine : edge.preRoutines)
            SemanticRoutines.call(routine, this);
        traverse(diagrams.get(edge.label));
        for (String routine : edge.postRoutines)
            SemanticRoutines.call(routine, this);
        return edge.nextNode;
    }

    private DiagramNode getNextNode(Diagram diagram, DiagramNode node) throws IOException {
        for (DiagramEdge edge : node.edges) {
            if (edge.isTerminal() && edge.label.equals(rawToken()))
                return matchTerminalEdge(edge);

            if (edge.label.equals("")) {
                if (exists(rawToken(), follow.get(diagram.nonTerminal)))
                        return edge.nextNode;
            } else if (!edge.isTerminal()) {
                if (exists(rawToken(), first.get(edge.label)))
                    return matchNonTerminalEdge(edge);
            }
        }

        for (DiagramEdge edge : node.edges)
            if (!edge.label.equals("") && !edge.isTerminal() &&
                    exists("", first.get(edge.label)) && exists(rawToken(), follow.get(edge.label)))
                return matchNonTerminalEdge(edge);

        // Error Handling
        DiagramEdge edge = node.edges.get(0);
        if (edge.isTerminal()) {
            if (edge.label.equals("$")) {
                errors.add(String.format("%d: Syntax Error! Malformed Input", lexer.getLineNumber()));
                setFinish();
                return null;
            }

            errors.add(String.format("%d: Syntax Error! Missing '%s'", lexer.getLineNumber(), edge.label));
            return matchTerminalEdge(edge);
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
                return matchNonTerminalEdge(edge);

            errors.add(String.format("%d: Syntax Error! Missing '%s' like '%s'", lexer.getLineNumber(), edge.label,
                    nonTerminalDescription(edge.label)));

            return edge.nextNode;
        }
    }

    private void traverse(Diagram diagram) throws IOException {
        DiagramNode node = diagram.start;
        while (node != diagram.finish && !finished) {
            node = getNextNode(diagram, node);
        }
    }

    public void getCode() throws IOException {
        traverse(diagrams.get("PROGRAM"));
    }
}
