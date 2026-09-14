package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Evidence that a parameter flows through a call chain into an operation
 * matching a predicate. Used by rules to build cross-method evidence chains.
 *
 * [resolutionConfidence] is the worst (most ambiguous) [ResolutionConfidence] seen while
 * resolving any hop of [steps] - a caller that only checks the confidence of its own direct
 * [CrossMethodResolver.resolve] call on the outermost call would miss an ambiguous overload
 * guess introduced two or more hops into the chain.
 */
public data class FlowEvidence(
    val paramName: String,
    val steps: List<FlowStep>,
    val terminal: FlowTarget,
    val resolutionConfidence: ResolutionConfidence,
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
     *
     * Precondition: relies entirely on [FunctionDecl.parameterFlows] having already been
     * populated by the parameter-flow annotation pass - an empty [callerFn].parameterFlows
     * (the pass hasn't run, or genuinely found no flows) short-circuits to null immediately,
     * it is not distinguished from "checked and found nothing".
     *
     * Guarantee: [call] must resolve to a [ResolutionResult.Resolved] via
     * [CrossMethodResolver.resolve] to be followed at all - an unresolved callee (dynamic
     * dispatch, external library call) returns null rather than guessing at the callee's
     * behavior. Unlike before, the resolution confidence at every hop *is* consulted now -
     * see [FlowEvidence.resolutionConfidence] - even though an
     * [ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS] match is still followed the same as
     * an exact one; only the reported confidence differs, not which flows get checked.
     *
     * Edge case: [maxDepth] bounds how many further calls are followed once inside the callee;
     * at `maxDepth <= 1` only the immediate callee's own flows are checked, deeper calls are
     * not traversed. Matching within the callee is by [FlowTarget.FunctionArgument.calledFunction]
     * name, not by argument position - see [findCallByNameAndLocation]'s kdoc for why location is
     * also needed to disambiguate same-named calls.
     */
    public fun parameterFlowsThrough(
        call: FunctionCall,
        callerFn: FunctionDecl,
        symbolTable: SymbolTable,
        maxDepth: Int = 2,
        predicate: (FlowTarget) -> Boolean,
    ): FlowEvidence? {
        val paramsPassed = paramsFlowingInto(callerFn.parameterFlows, call.name)
        if (paramsPassed.isEmpty()) return null

        val (resolved, confidence) = CrossMethodResolver.resolve(call, symbolTable).declAndConfidenceOrNull() ?: return null

        return paramsPassed.firstNotNullOfOrNull { callerFlow ->
            checkCalleeFlows(
                callerFlow.paramName,
                resolved,
                symbolTable,
                maxDepth,
                predicate,
                listOf(FlowStep(call.name, call.location)),
                confidence,
            )
        }
    }

    /** Which of [callerFlows] are passed as an argument to [calledFunction] - empty if none, or if [callerFlows] itself is empty. */
    private fun paramsFlowingInto(
        callerFlows: List<ParameterFlow>,
        calledFunction: String,
    ): List<ParameterFlow> =
        callerFlows.filter { flow ->
            flow.flowsInto.any { target -> target is FlowTarget.FunctionArgument && target.calledFunction == calledFunction }
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
                val resolved = CrossMethodResolver.resolve(call, symbolTable).declOrNull() ?: continue
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

    @Suppress("LoopWithTooManyJumpStatements") // Filter + continue is idiomatic for resolution chains
    private fun checkCalleeFlows(
        paramName: String,
        callee: FunctionDecl,
        symbolTable: SymbolTable,
        maxDepth: Int,
        predicate: (FlowTarget) -> Boolean,
        path: List<FlowStep>,
        confidenceSoFar: ResolutionConfidence,
    ): FlowEvidence? {
        // Check callee's own parameter flows for a match
        // Heuristic: match by name since we can't reliably determine arg position
        for (calleeFlow in callee.parameterFlows) {
            for (target in calleeFlow.flowsInto) {
                if (predicate(target)) {
                    return FlowEvidence(paramName = paramName, steps = path, terminal = target, resolutionConfidence = confidenceSoFar)
                }
                if (maxDepth <= 1 || target !is FlowTarget.FunctionArgument) continue

                val deeper = followIntoNextHop(paramName, callee, target, symbolTable, maxDepth, predicate, path, confidenceSoFar)
                if (deeper != null) return deeper
            }
        }
        return null
    }

    /** Follow one more level: the callee passes the param to yet another function - resolve and recurse into it. */
    @Suppress("LongParameterList") // Threading path/confidence through the recursive descent needs all of these
    private fun followIntoNextHop(
        paramName: String,
        callee: FunctionDecl,
        target: FlowTarget.FunctionArgument,
        symbolTable: SymbolTable,
        maxDepth: Int,
        predicate: (FlowTarget) -> Boolean,
        path: List<FlowStep>,
        confidenceSoFar: ResolutionConfidence,
    ): FlowEvidence? {
        val innerCall = findCallByNameAndLocation(callee, target.calledFunction, target.location) ?: return null
        val (innerResolved, innerConfidence) =
            CrossMethodResolver.resolve(innerCall, symbolTable).declAndConfidenceOrNull() ?: return null
        return checkCalleeFlows(
            paramName,
            innerResolved,
            symbolTable,
            maxDepth - 1,
            predicate,
            path + FlowStep(target.calledFunction, target.location),
            worstOf(confidenceSoFar, innerConfidence),
        )
    }

    private fun calleeIteratesParam(callee: FunctionDecl): Boolean =
        callee.parameterFlows.any { flow ->
            flow.flowsInto.any { it is FlowTarget.LoopIteration }
        }

    /**
     * Re-finds the exact call the [FlowTarget.FunctionArgument] was recorded from.
     * [ParameterFlowAnnotator][com.github.tvinke.algorilla.graph.ParameterFlowAnnotator]
     * constructs that FlowTarget directly from the call node's own name and location, so
     * matching on both here re-finds that same node deterministically - matching by name
     * alone (the previous behavior) picks the textually-first same-named call in the
     * callee body regardless of which one the flow was actually recorded from, the same
     * "name looks right, isn't proof" shape as the earlier recursion-family name-vs-type bugs.
     */
    private fun findCallByNameAndLocation(
        fn: FunctionDecl,
        name: String,
        location: SourceLocation,
    ): FunctionCall? = fn.findDescendants<FunctionCall>().firstOrNull { it.name == name && it.location == location }
}

/**
 * Multiple callees from the same caller iterate the same parameter.
 */
public data class RedundantIteration(
    val paramName: String,
    val iteratingCallees: List<String>,
)
