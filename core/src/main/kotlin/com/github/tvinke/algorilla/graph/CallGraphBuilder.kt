package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.util.CrossMethodResolver
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Constructs a [CallGraph] by walking IR trees, matching [FunctionCall] nodes
 * to known [FunctionDecl] entries in the [SymbolTable].
 *
 * Resolution strategy:
 * 1. Qualified name match (exact)
 * 2. Simple name match with parameter-count heuristic
 * 3. Simple name match (any)
 */
public class CallGraphBuilder(
    private val symbolTable: SymbolTable,
) {
    /**
     * Builds a call graph from the given IR trees.
     */
    public fun build(irTrees: Map<String, FileRoot>): CallGraph {
        val callGraph = CallGraph()
        for ((_, fileRoot) in irTrees) {
            collectEdges(fileRoot, null, callGraph)
        }
        logger.info { "Pass 2 complete: ${callGraph.edgeCount()} call edges" }
        return callGraph
    }

    private fun collectEdges(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        callGraph: CallGraph,
    ) {
        val currentFn = if (node is FunctionDecl) node else enclosingFn

        if (node is FunctionCall && currentFn != null) {
            resolveCallee(node, currentFn)?.let { callee ->
                callGraph.addEdge(currentFn.qualifiedName, callee.qualifiedName)
            }
        }

        for (child in node.children) {
            collectEdges(child, currentFn, callGraph)
        }
    }

    private fun resolveCallee(
        call: FunctionCall,
        enclosingFn: FunctionDecl,
    ): FunctionDecl? = CrossMethodResolver.resolve(call, symbolTable, enclosingClass = enclosingFn.declaringClass)
}
