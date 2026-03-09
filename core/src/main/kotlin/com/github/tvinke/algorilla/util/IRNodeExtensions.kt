package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.CollectionSemanticsRegistry

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

private val registryInstance: CollectionSemanticsRegistry by lazy {
    CollectionSemanticsRegistry.loadDefaults()
}
