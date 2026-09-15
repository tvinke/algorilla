package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LoopNode

/**
 * Walks this IR (sub)tree, tracking the enclosing [FunctionDecl] and the stack of [LoopNode]s
 * a node is nested in, and invokes [visit] for every node that sits inside at least one loop.
 *
 * This is the shared scaffold behind the "loop-amplifier" rules (hidden nested loops, N+1
 * queries, regex recompilation, etc.): push a [LoopNode] onto the stack, recurse into its
 * children, and call [visit] for everything found underneath. Callers filter [visit] on the
 * node type(s) they care about (e.g. `if (node is FunctionCall) ...`) — the walker itself
 * doesn't need to know which node kinds a given rule dispatches on.
 */
public fun IRNode.walkLoopSites(visit: (node: IRNode, enclosingFn: FunctionDecl?, loopStack: List<LoopNode>) -> Unit) {
    fun scan(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scan(child, fn, loopStack + node)
            }
            return
        }

        if (loopStack.isNotEmpty()) visit(node, fn, loopStack)

        for (child in node.children) {
            scan(child, fn, loopStack)
        }
    }
    scan(this, null, emptyList())
}
