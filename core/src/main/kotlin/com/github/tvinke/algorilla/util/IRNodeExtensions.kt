package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

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

/**
 * Branch context for an IR node: maps each ancestor [BranchNode] (by identity hash)
 * to the branch index the node resides in. Two nodes with incompatible contexts
 * are in mutually exclusive branches and cannot co-execute.
 */
public typealias BranchContext = Map<Int, Int>

/**
 * Finds all descendant nodes of the specified type together with their [BranchContext].
 * The branch context tracks which branch of each ancestor [BranchNode] the descendant is in.
 */
public inline fun <reified T : IRNode> IRNode.findDescendantsWithBranchContext(): List<Pair<T, BranchContext>> {
    val results = mutableListOf<Pair<T, BranchContext>>()
    val stack = ArrayDeque<Pair<IRNode, BranchContext>>()
    stack.addFirst(this to emptyMap())
    while (stack.isNotEmpty()) {
        val (node, context) = stack.removeFirst()
        if (node is T) {
            results.add(node to context)
        }
        pushChildrenWithContext(node, context, stack)
    }
    return results
}

@PublishedApi
internal fun pushChildrenWithContext(
    node: IRNode,
    context: BranchContext,
    stack: ArrayDeque<Pair<IRNode, BranchContext>>,
) {
    if (node is BranchNode) {
        for ((branchIndex, branch) in node.branches.withIndex()) {
            val childContext = context + (System.identityHashCode(node) to branchIndex)
            for (child in branch.asReversed()) {
                stack.addFirst(child to childContext)
            }
        }
    } else {
        for (child in node.children.asReversed()) {
            stack.addFirst(child to context)
        }
    }
}

/**
 * Returns true if two branch contexts are compatible, meaning the nodes can co-execute.
 * Contexts are incompatible when they disagree on the branch index for any shared [BranchNode].
 */
public fun BranchContext.isCompatibleWith(other: BranchContext): Boolean {
    for ((nodeId, idx) in this) {
        val otherIdx = other[nodeId]
        if (otherIdx != null && otherIdx != idx) return false
    }
    return true
}

/**
 * From a list of items with branch contexts, finds the largest subset
 * where all items can co-execute (pairwise compatible branch contexts).
 * For typical code (small groups), this is efficient enough.
 */
public fun <T> maxCoExecutableSubset(items: List<Pair<T, BranchContext>>): List<T> {
    if (items.size <= 1) return items.map { it.first }

    // Group by exact branch context — items in the same context always co-execute
    val byContext = items.groupBy { it.second }

    // Find the largest group of items from compatible contexts
    val contextGroups = byContext.entries.toList()
    var bestSubset = emptyList<T>()

    for (i in contextGroups.indices) {
        val compatible = mutableListOf<T>()
        compatible.addAll(contextGroups[i].value.map { it.first })
        val baseContext = contextGroups[i].key

        for (j in contextGroups.indices) {
            if (j == i) continue
            if (baseContext.isCompatibleWith(contextGroups[j].key)) {
                compatible.addAll(contextGroups[j].value.map { it.first })
            }
        }

        if (compatible.size > bestSubset.size) {
            bestSubset = compatible
        }
    }

    return bestSubset
}

/**
 * Checks if a variable name corresponds to an O(1) lookup type based on parameter or variable declarations.
 * Delegates to the semantics registry for O(1) type detection.
 */
public fun FunctionDecl.hasO1Type(variableName: String?): Boolean {
    if (variableName == null) return false
    val registry = registryInstance
    val paramType = parameters.find { it.name == variableName }?.typeName
    if (paramType != null && registry.isO1Type(paramType)) return true
    val varType = findDescendants<VariableDecl>().find { it.name == variableName }?.typeName
    return varType != null && registry.isO1Type(varType)
}

/**
 * Returns true if this [LookupCall] targets a collection (worth flagging).
 * A lookup is a collection lookup when it's not O(1), not on a scalar type,
 * and the enclosing function doesn't declare the target as an O(1) type.
 */
public fun LookupCall.isCollectionLookup(fn: FunctionDecl?): Boolean = !isO1 && !isScalar && (fn == null || !fn.hasO1Type(targetVariable))

/**
 * Returns true if [node] textually references [name] in any of its identifying fields.
 * This is a heuristic: it matches variable/target names on IR nodes, not a true
 * data-flow analysis. False negatives occur when names are aliased or transformed.
 * False positives are rare because IR field names come directly from the source code.
 */
public fun IRNode.referencesName(name: String): Boolean =
    when (this) {
        is LoopNode -> iteratedVariable == name
        is LookupCall -> targetVariable == name
        is FunctionCall -> qualifiedTarget == name || (qualifiedTarget == null && this.name == name)
        is VariableDecl -> this.name == name
        else -> false
    }

/**
 * Returns true if the function calls itself directly (recursive method).
 * Recursive methods (tree walkers, visitors, DFS) contain loops that iterate
 * child nodes — total work is O(tree_size), not O(n²).
 */
public fun FunctionDecl.isRecursive(): Boolean = findDescendants<FunctionCall>().any { it.name == name }

/**
 * Recursively transforms an IR tree by applying [fn] to each node bottom-up.
 * Only rebuilds the path where nodes actually changed.
 */
public fun IRNode.transform(fn: (IRNode) -> IRNode): IRNode {
    val transformed = fn(this)
    val newChildren = transformed.children.map { it.transform(fn) }
    return if (newChildren == transformed.children) transformed else transformed.withChildren(newChildren)
}

/**
 * Returns a copy of this node with the given children list.
 * Uses data class copy() for each concrete IR node type.
 */
@Suppress("CyclomaticComplexMethod")
public fun IRNode.withChildren(newChildren: List<IRNode>): IRNode =
    when (this) {
        is LoopNode -> copy(children = newChildren)
        is LookupCall -> copy(children = newChildren)
        is SortCall -> copy(children = newChildren)
        is ObjectCreation -> copy(children = newChildren)
        is CollectionAccess -> copy(children = newChildren)
        is FunctionDecl -> copy(children = newChildren)
        is FunctionCall -> copy(children = newChildren)
        is VariableDecl -> copy(children = newChildren)
        is BranchNode -> copy(branches = listOf(newChildren))
        is GenericNode -> copy(children = newChildren)
        is FileRoot -> copy(children = newChildren)
    }

private val registryInstance: LanguageSemanticsRegistry by lazy {
    LanguageSemanticsRegistry.loadDefaults()
}
