package Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Parser {
    private static Map<String, Diagram> diagrams;

    private static void initialize() throws FileNotFoundException {
        diagrams = new HashMap<>();

        Scanner grammarScanner = new Scanner(new File("grammar.txt"));
        while (grammarScanner.hasNext()) {
            String nonTerminal = grammarScanner.next();
            grammarScanner.next();

            Diagram diagram = new Diagram();
            diagrams.put(nonTerminal, diagram);

            boolean hasNext = true;
            while (hasNext)
                hasNext = diagram.createPath(grammarScanner);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        initialize();

        Diagram diagram = diagrams.get("DEFAULT_STMT");
        Queue<DiagramNode> q = new LinkedList<>();
        q.add(diagram.start);
        while (!q.isEmpty()) {
            for (DiagramEdge edge : q.peek().edges) {
                q.add(edge.nextNode);
                System.out.println(q.peek().hashCode() + " " + edge.label + " " + edge.nextNode.hashCode());
            }
            q.remove();
        }
    }
}
