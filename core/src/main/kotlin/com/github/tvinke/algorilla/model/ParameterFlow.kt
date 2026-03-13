package com.github.tvinke.algorilla.model

/**
 * Describes how a function parameter flows into operations within the function body.
 * Computed by the [com.github.tvinke.algorilla.graph.ParameterFlowAnnotator] pass and
 * stored on [FunctionDecl.parameterFlows].
 *
 * This is a heuristic analysis: it tracks where parameter names appear in the IR tree
 * fields (iteratedVariable, targetVariable, qualifiedTarget), not a formal data-flow proof.
 */
public data class ParameterFlow(
    val paramIndex: Int,
    val paramName: String,
    val flowsInto: Set<FlowTarget>,
)

/**
 * A location in the function body where a parameter's value is consumed.
 */
public sealed interface FlowTarget {
    /** The parameter is iterated over in a loop (for-each, stream forEach, etc.). */
    public data class LoopIteration(
        val location: SourceLocation,
    ) : FlowTarget

    /** The parameter is passed as an argument to another function call. */
    public data class FunctionArgument(
        val calledFunction: String,
        val location: SourceLocation,
    ) : FlowTarget

    /** The parameter is used as the receiver of a method call (e.g. param.contains()). */
    public data class MethodCallReceiver(
        val methodName: String,
        val location: SourceLocation,
    ) : FlowTarget
}
