class TreeNode(val value: Int, val children: List<TreeNode> = emptyList())

class TreeWalker {
    fun collectValues(node: TreeNode): List<Int> {
        val result = mutableListOf(node.value)
        for (child in node.children) {
            result.addAll(collectValues(child))
        }
        return result
    }
}
