package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.SourceLocation
import org.treesitter.TSNode

// Shared traversal scaffolding for the tree-sitter-based visitors (JavaScript/TypeScript,
// Kotlin). Java and Groovy go through ANTLR instead, see VisitorSupport.kt for their shared
// bits - nothing here applies to them. The rest of visit() (the actual node-type dispatch and
// what IR each node type produces) stayed put in each visitor: that part is genuinely
// different per language, only the generic "walk this TSNode" plumbing below was identical.

/** Visits every named child of [node] with [visit] and flattens the results. */
public inline fun visitChildren(
    node: TSNode,
    visit: (TSNode) -> List<IRNode>,
): List<IRNode> {
    val result = mutableListOf<IRNode>()
    for (i in 0 until node.namedChildCount) {
        val child = node.getNamedChild(i)
        if (!child.isNull) {
            result.addAll(visit(child))
        }
    }
    return result
}

/** Extracts [node]'s source text via its byte range, clamped to [source]'s length. */
public fun nodeText(
    node: TSNode,
    source: String,
): String {
    if (node.isNull) return ""
    val start = node.startByte.coerceAtMost(source.length)
    val end = node.endByte.coerceAtMost(source.length)
    return source.substring(start, end)
}

/** Converts [node]'s 0-based tree-sitter start point into a 1-based [SourceLocation]. */
public fun locationOf(
    node: TSNode,
    filePath: String,
): SourceLocation = SourceLocation(filePath, node.startPoint.row + 1, node.startPoint.column + 1)
