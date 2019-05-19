package Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Parser {
    private static Map<String, Diagram> diagrams;
    private static Map<String, List<String>> first;
    private static Map<String, List<String>> follow;

    private static void initializeDiagram() throws FileNotFoundException {
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

    public static void main(String[] args) throws FileNotFoundException {
        initializeDiagram();

        first = new HashMap<>();
        follow = new HashMap<>();

        // First and follow are derived from http://smlweb.cpsc.ucalgary.ca
        initializeSet(first, "first.txt");
        initializeSet(follow, "follow.txt");

//        for (String firstValue : first.get("EXPRESSION_STMT"))
//            System.out.println(firstValue);

//        Diagram diagram = diagrams.get("DEFAULT_STMT");
//        Queue<DiagramNode> q = new LinkedList<>();
//        q.add(diagram.start);
//        while (!q.isEmpty()) {
//            for (DiagramEdge edge : q.peek().edges) {
//                q.add(edge.nextNode);
//                System.out.println(q.peek().hashCode() + " " + edge.label + " " + edge.nextNode.hashCode());
//            }
//            q.remove();
//        }
    }
}
