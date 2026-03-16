package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.Complexity
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SortCall
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Pass 3: annotates [FunctionDecl] nodes with estimated complexity and execution context.
 *
 * Bottom-up propagation: functions that are called from within loops get marked as
 * [ExecutionContext.INSIDE_REPEATED_CALL_FROM_LOOP], so rules can detect cross-file patterns
 * where a seemingly innocent function call inside a loop delegates to an expensive operation.
 */
public class ComplexityAnnotator(
    private val symbolTable: SymbolTable,
    private val callGraph: CallGraph,
    @Suppress("UnusedPrivateProperty") // Reserved for depth-bounded cross-function complexity propagation
    private val maxCallDepth: Int,
) {
    /**
     * Annotates all function declarations with estimated complexity and execution context.
     */
    public fun annotate() {
        val allDecls = symbolTable.allDeclarations()
        for (decl in allDecls) {
            decl.estimatedComplexity = estimateComplexity(decl)
        }
        propagateExecutionContext(allDecls)
        logger.info { "Pass 3 complete: ${allDecls.size} functions annotated" }
    }

    private fun estimateComplexity(decl: FunctionDecl): Complexity {
        val hasLoop = hasDescendant<LoopNode>(decl)
        val hasNestedLookup = hasLoopWithLookup(decl)
        val hasSort = hasDescendant<SortCall>(decl)

        return when {
            hasNestedLookup -> Complexity("O(n*m)", "loop with linear lookup")
            hasSort && hasLoop -> Complexity("O(n * n log n)", "sort inside loop")
            hasSort -> Complexity("O(n log n)", "sort operation")
            hasLoop -> Complexity("O(n)", "loop")
            else -> Complexity("O(1)", "constant")
        }
    }

    private fun propagateExecutionContext(allDecls: List<FunctionDecl>) {
        val declsByName = allDecls.associateBy { it.qualifiedName }
        for (decl in allDecls) {
            if (isCalledFromLoop(decl, declsByName)) {
                decl.executionContext = ExecutionContext.INSIDE_REPEATED_CALL_FROM_LOOP
            }
        }
    }

    private fun isCalledFromLoop(
        decl: FunctionDecl,
        declsByName: Map<String, FunctionDecl>,
    ): Boolean {
        val callerNames = callGraph.callers(decl.qualifiedName)
        for (callerName in callerNames) {
            val caller = declsByName[callerName] ?: continue
            if (callsFromInsideLoop(caller, decl.name)) return true
        }
        return false
    }

    private fun callsFromInsideLoop(
        caller: FunctionDecl,
        calleeName: String,
    ): Boolean = searchForCallInLoop(caller.children, calleeName, insideLoop = false)

    private fun searchForCallInLoop(
        nodes: List<IRNode>,
        calleeName: String,
        insideLoop: Boolean,
    ): Boolean {
        for (node in nodes) {
            if (node is LoopNode) {
                if (searchForCallInLoop(node.children, calleeName, insideLoop = true)) return true
            } else if (insideLoop && node is com.github.tvinke.algorilla.model.FunctionCall && node.name == calleeName) {
                return true
            } else {
                if (searchForCallInLoop(node.children, calleeName, insideLoop)) return true
            }
        }
        return false
    }

    private fun hasLoopWithLookup(decl: FunctionDecl): Boolean = searchForLookupInLoop(decl.children, insideLoop = false)

    private fun searchForLookupInLoop(
        nodes: List<IRNode>,
        insideLoop: Boolean,
    ): Boolean {
        for (node in nodes) {
            when {
                node is LoopNode -> if (searchForLookupInLoop(node.children, insideLoop = true)) return true
                insideLoop && node is LookupCall && !node.isO1 -> return true
                else -> if (searchForLookupInLoop(node.children, insideLoop)) return true
            }
        }
        return false
    }

    private inline fun <reified T : IRNode> hasDescendant(node: IRNode): Boolean {
        val stack = ArrayDeque<IRNode>()
        stack.addAll(node.children)
        while (stack.isNotEmpty()) {
            val current = stack.removeFirst()
            if (current is T) return true
            stack.addAll(0, current.children)
        }
        return false
    }
}
