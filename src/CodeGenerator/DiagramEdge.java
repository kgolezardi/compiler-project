package CodeGenerator;

import java.util.ArrayList;
import java.util.List;

class DiagramEdge {
    final String label;
    final DiagramNode nextNode;
    final List<String> preRoutines, postRoutines;

    boolean isTerminal() {
        return label.length() > 0 && !Character.isUpperCase(label.charAt(0));
    }

    DiagramEdge(String label, DiagramNode nextNode, List<String> preRoutines, List<String> postRoutines) {
        this.label = label;
        this.nextNode = nextNode;
        this.preRoutines = new ArrayList<>(preRoutines);
        this.postRoutines = new ArrayList<>(postRoutines);
    }
}
