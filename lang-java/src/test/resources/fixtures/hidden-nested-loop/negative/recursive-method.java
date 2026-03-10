import java.util.List;

public class TreeWalker {
    public void walkAll(List<TreeNode> roots) {
        for (TreeNode root : roots) {
            visit(root);
        }
    }

    private void visit(TreeNode node) {
        process(node);
        for (TreeNode child : node.getChildren()) {
            visit(child);  // recursive — should not cause infinite loop in analysis
        }
    }

    private void process(TreeNode node) {
        // no loop
    }
}
