package com.github.tvinke.algorilla.graph

/**
 * Directed graph representing caller-to-callee relationships across the project.
 * Used for cross-file complexity propagation.
 */
public class CallGraph {
    private val edges: MutableMap<String, MutableSet<String>> = mutableMapOf()
    private val reverseEdges: MutableMap<String, MutableSet<String>> = mutableMapOf()

    /**
     * Adds a call edge from [callerQualifiedName] to [calleeQualifiedName].
     */
    public fun addEdge(
        callerQualifiedName: String,
        calleeQualifiedName: String,
    ) {
        edges.getOrPut(callerQualifiedName) { mutableSetOf() }.add(calleeQualifiedName)
        reverseEdges.getOrPut(calleeQualifiedName) { mutableSetOf() }.add(callerQualifiedName)
    }

    /**
     * Returns all functions called by the given function.
     */
    public fun callees(qualifiedName: String): Set<String> = edges[qualifiedName] ?: emptySet()

    /**
     * Returns all functions that call the given function.
     */
    public fun callers(qualifiedName: String): Set<String> = reverseEdges[qualifiedName] ?: emptySet()

    /**
     * Returns the total number of call edges.
     */
    public fun edgeCount(): Int = edges.values.sumOf { it.size }
}
