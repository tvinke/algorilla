package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.declOrNull
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
            collectEdges(fileRoot, null, fileRoot.language, callGraph)
        }
        logger.info { "Pass 2 complete: ${callGraph.edgeCount()} call edges" }
        return callGraph
    }

    private fun collectEdges(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        language: Language,
        callGraph: CallGraph,
    ) {
        val currentFn = if (node is FunctionDecl) node else enclosingFn

        if (node is FunctionCall && currentFn != null) {
            resolveCallee(node, currentFn, language)?.let { callee ->
                callGraph.addEdge(currentFn.qualifiedName, callee.qualifiedName)
            }
        }

        for (child in node.children) {
            collectEdges(child, currentFn, language, callGraph)
        }
    }

    private fun resolveCallee(
        call: FunctionCall,
        enclosingFn: FunctionDecl,
        language: Language,
    ): FunctionDecl? =
        // The call graph doesn't track resolution confidence - an ambiguous overload guess
        // is still the best edge we have to offer between two functions. [language] (the IR
        // tree's own FileRoot.language, not a hardcoded default) picks the right
        // unresolvable-names skiplist - see resolve()'s kdoc: passing the wrong language here
        // means a Kotlin/Groovy/JS stdlib call absent from Java's skiplist can be mistaken for
        // a call to a coincidentally-named local function (a false edge), while a name that's
        // only unresolvable in Java can wrongly suppress a real edge to an actual same-named
        // function in another language.
        CrossMethodResolver.resolve(call, symbolTable, language, enclosingClass = enclosingFn.declaringClass).declOrNull()
}
