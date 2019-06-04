package CodeGenerator;

import java.util.ArrayList;
import java.util.List;

class DiagramNode {
    private boolean isFinal;
    List<DiagramEdge> edges;

    DiagramNode(boolean isFinal) {
        this.isFinal = isFinal;
        edges = new ArrayList<>();
    }
}
