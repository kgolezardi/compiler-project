package CodeGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Diagram {
    public final String nonTerminal;
    public final DiagramNode start, finish;

    public Diagram(String nonTerminal) {
        this.nonTerminal = nonTerminal;
        start = new DiagramNode(false);
        finish = new DiagramNode(true);
    }

    /* Only first edge has pre routines */
    public boolean createPath(Scanner scanner) {
        String lastLabel = "";
        DiagramNode previousNode = null;
        List<String> preRoutines = new ArrayList<>();
        List<String> postRoutines = new ArrayList<>();

        while (true) {
            String token = null;
            do {
                if (token != null)
                    postRoutines.add(token);
                token = scanner.next();
            } while (token.charAt(0) == '#');

            if (token.equals("|") || token.equals(".")) {
                if (previousNode == null) {
                    start.edges.add(new DiagramEdge("", finish, preRoutines, postRoutines));
                }
                else {
                    DiagramEdge edge = new DiagramEdge(lastLabel, finish, preRoutines, postRoutines);
                    previousNode.edges.add(edge);
                }

                return token.equals("|");
            }
            if (previousNode == null) {
                previousNode = start;
                preRoutines.addAll(postRoutines);
                postRoutines.clear();
            }
            else {
                DiagramNode node = new DiagramNode(false);
                DiagramEdge edge = new DiagramEdge(lastLabel, node, preRoutines, postRoutines);
                preRoutines.clear();
                postRoutines.clear();
                previousNode.edges.add(edge);
                previousNode = node;
            }
            lastLabel = token;
        }
    }
}
