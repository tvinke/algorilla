package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Evidence that a parameter flows through a call chain into an operation
 * matching a predicate. Used by rules to build cross-method evidence chains.
 */
public data class FlowEvidence(
    val paramName: String,
    val steps: List<FlowStep>,
    val terminal: FlowTarget,
)

/**
 * A single step in a parameter flow chain: the parameter (or its alias)
 * was passed to [calledFunction] at [location].
 */
public data class FlowStep(
    val calledFunction: String,
    val location: SourceLocation,
)

/**
 * Queries parameter-flow annotations to answer cross-method questions.
 * Rules use this instead of traversing [FunctionDecl.parameterFlows] directly.
 */
public object ParameterFlowQuery {
    /**
     * Checks whether a parameter of [callerFn] flows through [call] into an operation
     * matching [predicate] in the resolved callee (or its transitive callees, up to [maxDepth]).
     *
     * Returns [FlowEvidence] describing the flow path, or null if no matching flow exists.
     */
    public fun parameterFlowsThrough(
        call: FunctionCall,
        callerFn: FunctionDecl,
        symbolTable: SymbolTable,
        maxDepth: Int = 2,
        predicate: (FlowTarget) -> Boolean,
    ): FlowEvidence? {
        // Which caller parameters are passed as arguments to this call?
        val callerFlows = callerFn.parameterFlows
        if (callerFlows.isEmpty()) return null

        val paramsPassed =
            callerFlows.filter { flow ->
                flow.flowsInto.any { target ->
                    target is FlowTarget.FunctionArgument && target.calledFunction == call.name
                }
            }
        if (paramsPassed.isEmpty()) return null

        val resolved = CrossMethodResolver.resolve(call, symbolTable) ?: return null

        for (callerFlow in paramsPassed) {
            val evidence =
                checkCalleeFlows(
                    callerFlow.paramName,
                    resolved,
                    symbolTable,
                    maxDepth,
                    predicate,
                    listOf(FlowStep(call.name, call.location)),
                )
            if (evidence != null) return evidence
        }
        return null
    }

    /**
     * Finds cases where multiple callees from [callerFn] all iterate the same parameter.
     * Returns a list of (paramName, list of callee names that iterate it).
     */
    @Suppress("LoopWithTooManyJumpStatements") // Filter + continue is idiomatic for resolution chains
    public fun findRedundantIterations(
        callerFn: FunctionDecl,
        calls: List<FunctionCall>,
        symbolTable: SymbolTable,
    ): List<RedundantIteration> {
        val callerFlows = callerFn.parameterFlows
        if (callerFlows.isEmpty()) return emptyList()

        val results = mutableListOf<RedundantIteration>()

        for (flow in callerFlows) {
            // Find all callees that receive this parameter as an argument
            val calleeNames =
                flow.flowsInto
                    .filterIsInstance<FlowTarget.FunctionArgument>()
                    .map { it.calledFunction }
                    .toSet()

            // For each callee, check if it iterates the received parameter
            val iteratingCallees = mutableListOf<String>()
            for (call in calls) {
                if (call.name !in calleeNames) continue
                val resolved = CrossMethodResolver.resolve(call, symbolTable) ?: continue
                if (calleeIteratesParam(resolved)) {
                    iteratingCallees.add(call.name)
                }
            }

            if (iteratingCallees.size >= 2) {
                results.add(RedundantIteration(flow.paramName, iteratingCallees))
            }
        }

        return results
    }

    @Suppress("NestedBlockDepth", "LoopWithTooManyJumpStatements") // Flow traversal requires nested checks
    private fun checkCalleeFlows(
        paramName: String,
        callee: FunctionDecl,
        symbolTable: SymbolTable,
        maxDepth: Int,
        predicate: (FlowTarget) -> Boolean,
        path: List<FlowStep>,
    ): FlowEvidence? {
        // Check callee's own parameter flows for a match
        // Heuristic: match by name since we can't reliably determine arg position
        val calleeFlows = callee.parameterFlows
        for (calleeFlow in calleeFlows) {
            for (target in calleeFlow.flowsInto) {
                if (predicate(target)) {
                    return FlowEvidence(paramName = paramName, steps = path, terminal = target)
                }

                // Follow one more level: if the callee passes the param to yet another function
                if (maxDepth > 1 && target is FlowTarget.FunctionArgument) {
                    val innerCall = findCallByName(callee, target.calledFunction) ?: continue
                    val innerResolved = CrossMethodResolver.resolve(innerCall, symbolTable) ?: continue
                    val deeper =
                        checkCalleeFlows(
                            paramName,
                            innerResolved,
                            symbolTable,
                            maxDepth - 1,
                            predicate,
                            path + FlowStep(target.calledFunction, target.location),
                        )
                    if (deeper != null) return deeper
                }
            }
        }
        return null
    }

    private fun calleeIteratesParam(callee: FunctionDecl): Boolean =
        callee.parameterFlows.any { flow ->
            flow.flowsInto.any { it is FlowTarget.LoopIteration }
        }

    private fun findCallByName(
        fn: FunctionDecl,
        name: String,
    ): FunctionCall? = fn.findDescendants<FunctionCall>().firstOrNull { it.name == name }
}

/**
 * Multiple callees from the same caller iterate the same parameter.
 */
public data class RedundantIteration(
    val paramName: String,
    val iteratingCallees: List<String>,
)
