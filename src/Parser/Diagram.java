package Parser;

import java.util.Scanner;

public class Diagram {
    DiagramNode start, finish;

    public Diagram() {
        start = new DiagramNode(false);
        finish = new DiagramNode(true);
    }

    public boolean createPath(Scanner scanner) {
        String lastLabel = "";
        DiagramNode previousNode = null;
        while (true) {
            String token = scanner.next();
            if (token.equals("|") || token.equals(".")) {
                if (previousNode == null) {
                    start.edges.add(new DiagramEdge("", finish));
                }
                else {
                    DiagramEdge edge = new DiagramEdge(lastLabel, finish);
                    previousNode.edges.add(edge);
                }

                return token.equals("|");
            }
            if (previousNode == null) {
                previousNode = start;
            }
            else {
                DiagramNode node = new DiagramNode(false);
                DiagramEdge edge = new DiagramEdge(lastLabel, node);
                previousNode.edges.add(edge);
                previousNode = node;
            }
            lastLabel = token;
        }
    }
}
