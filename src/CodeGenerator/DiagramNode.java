package CodeGenerator;

import java.util.ArrayList;
import java.util.List;

public class DiagramNode {
    public boolean isFinal;
    public List<DiagramEdge> edges;

    public DiagramNode(boolean isFinal) {
        this.isFinal = isFinal;
        edges = new ArrayList<>();
    }
}
