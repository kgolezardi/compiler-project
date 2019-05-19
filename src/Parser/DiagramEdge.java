package Parser;

public class DiagramEdge {
    //public boolean isTerminal;
    public String label;
    public DiagramNode nextNode;

    public DiagramEdge(String label, DiagramNode nextNode) {
        this.label = label;
        this.nextNode = nextNode;
    }
}
