class TreeWalker {
    void traverse(Node node) {
        process(node);
        traverse(node.getLeft());
        traverse(node.getRight());
    }
}
