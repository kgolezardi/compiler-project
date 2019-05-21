package Parser;

public class DiagramEdge {
    public final String label;
    public final DiagramNode nextNode;

    public boolean isTerminal() {
        return label.length() > 0 && !Character.isUpperCase(label.charAt(0));
    }

    public DiagramEdge(String label, DiagramNode nextNode) {
        this.label = label;
        this.nextNode = nextNode;
    }
}
