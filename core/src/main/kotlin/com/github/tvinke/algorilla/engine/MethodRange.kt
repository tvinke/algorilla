package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.util.findDescendants

/**
 * A method's source-line span, keyed by its qualified name - used to resolve a
 * [com.github.tvinke.algorilla.rules.Finding]'s location back to its enclosing method.
 *
 * Shared by cache persistence ([AnalysisEngine]) and issue grouping ([groupFindings]), which
 * both need this same mapping. They previously built two separate copies with subtly different
 * (and in the cache's case, wrong for nested functions) enclosing-method semantics - see
 * [findEnclosingMethod].
 */
internal data class MethodRange(
    val qualifiedName: String,
    val startLine: Int,
    val endLine: Int,
)

/** Builds file path → [MethodRange]s for every function declared in [irTrees]. */
internal fun buildMethodRangeIndex(irTrees: Map<String, FileRoot>): Map<String, List<MethodRange>> {
    val result = mutableMapOf<String, MutableList<MethodRange>>()
    for ((_, fileRoot) in irTrees) {
        val ranges = result.getOrPut(fileRoot.filePath) { mutableListOf() }
        for (fn in fileRoot.findDescendants<FunctionDecl>()) {
            ranges.add(MethodRange(fn.qualifiedName, fn.location.line, maxLineOf(fn)))
        }
    }
    return result
}

/**
 * Finds the smallest range in [rangesInFile] containing [line] - the innermost enclosing method
 * when functions nest (a local function/lambda declared inside another). Returns null when
 * [line] isn't inside any known range.
 */
internal fun findEnclosingMethod(
    rangesInFile: List<MethodRange>,
    line: Int,
): String? =
    rangesInFile
        .filter { line in it.startLine..it.endLine }
        .minByOrNull { it.endLine - it.startLine }
        ?.qualifiedName

/** Recursively finds the maximum source line in an IR subtree. */
internal fun maxLineOf(node: IRNode): Int {
    var max = node.location.line
    for (child in node.children) {
        val childMax = maxLineOf(child)
        if (childMax > max) max = childMax
    }
    return max
}
