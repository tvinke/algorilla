import java.util.List;
import java.util.ArrayList;

/**
 * Tree-walking recursive method that uses forEach on children.
 * This is O(tree_size), not O(n^2) — the forEach visits each child once.
 * Should NOT trigger expensive-callback because the method is recursive.
 */
public class RecursiveTreeWalkCallback {

    static class TreeNode {
        String name;
        List<TreeNode> children;

        TreeNode(String name, List<TreeNode> children) {
            this.name = name;
            this.children = children;
        }
    }

    public List<String> collectNames(TreeNode node) {
        List<String> result = new ArrayList<>();
        result.add(node.name);
        node.children.forEach(child -> {
            result.addAll(collectNames(child));
        });
        return result;
    }

    public void printTree(TreeNode node, int depth) {
        String indent = " ".repeat(depth);
        System.out.println(indent + node.name);
        node.children.forEach(child -> printTree(child, depth + 1));
    }
}
