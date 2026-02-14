package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.IRNode

/**
 * Finds all descendant nodes of the specified type in this IR tree.
 */
public inline fun <reified T : IRNode> IRNode.findDescendants(): List<T> {
    val results = mutableListOf<T>()
    val stack = ArrayDeque<IRNode>()
    stack.addAll(this.children)
    while (stack.isNotEmpty()) {
        val node = stack.removeFirst()
        if (node is T) {
            results.add(node)
        }
        stack.addAll(0, node.children)
    }
    return results
}
