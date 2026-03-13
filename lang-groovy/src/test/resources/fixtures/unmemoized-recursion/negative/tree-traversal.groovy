class TreeWalker {
    void traverse(Object node) {
        if (node == null) return;
        traverse(node.getLeft());
        traverse(node.getRight());
    }
}
