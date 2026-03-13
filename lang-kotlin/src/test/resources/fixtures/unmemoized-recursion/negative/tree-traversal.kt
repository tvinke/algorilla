class TreeWalker {
    fun traverse(node: Node) {
        traverse(node.getLeft())
        traverse(node.getRight())
    }
}
