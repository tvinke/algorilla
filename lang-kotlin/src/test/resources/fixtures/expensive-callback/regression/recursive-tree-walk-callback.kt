/**
 * Tree-walking recursive function that uses forEach on children.
 * This is O(tree_size), not O(n^2) — the forEach visits each child once.
 * Should NOT trigger expensive-callback because the function is recursive.
 */
class TreeNode(val name: String, val children: List<TreeNode>)

class TreeWalker {
    fun collectNames(node: TreeNode): List<String> {
        val result = mutableListOf(node.name)
        node.children.forEach { child ->
            result.addAll(collectNames(child))
        }
        return result
    }

    fun printTree(node: TreeNode, depth: Int) {
        println(" ".repeat(depth) + node.name)
        node.children.forEach { child -> printTree(child, depth + 1) }
    }
}
