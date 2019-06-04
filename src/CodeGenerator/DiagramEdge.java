package CodeGenerator;

import java.util.ArrayList;
import java.util.List;

public class DiagramEdge {
    public final String label;
    public final DiagramNode nextNode;
    public final List<String> preRoutines, postRoutines;

    public boolean isTerminal() {
        return label.length() > 0 && !Character.isUpperCase(label.charAt(0));
    }

    public DiagramEdge(String label, DiagramNode nextNode, List<String> preRoutines, List<String> postRoutines) {
        this.label = label;
        this.nextNode = nextNode;
        this.preRoutines = new ArrayList<>(preRoutines);
        this.postRoutines = new ArrayList<>(postRoutines);
    }
}
