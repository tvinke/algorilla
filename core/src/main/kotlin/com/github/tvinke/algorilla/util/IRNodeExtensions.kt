package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.VariableDecl

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

private val O1_TYPE_INDICATORS = setOf("Set", "Map", "HashMap", "HashSet", "TreeMap", "TreeSet", "LinkedHashMap", "LinkedHashSet")

/**
 * Checks if a variable name corresponds to an O(1) lookup type based on parameter or variable declarations.
 */
public fun FunctionDecl.hasO1Type(variableName: String?): Boolean {
    if (variableName == null) return false
    val paramType = parameters.find { it.name == variableName }?.typeName
    if (paramType != null && O1_TYPE_INDICATORS.any { paramType.contains(it) }) return true
    val varType = findDescendants<VariableDecl>().find { it.name == variableName }?.typeName
    return varType != null && O1_TYPE_INDICATORS.any { varType.contains(it) }
}
