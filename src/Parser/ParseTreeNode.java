package Parser;

import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {
    public String label;
    public List<ParseTreeNode> children;

    public ParseTreeNode(String label) {
        this.label = label;
        children = new ArrayList<>();
    }
}
