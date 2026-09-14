package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign (cc #73) - the exact "self-call-by-name"
 * shape from the original #64/#71 recursion-family bug (matching a call by bare name
 * instead of confirming it's really the same one), now found in a different helper.
 *
 * findCallByName re-finds "the inner call this FlowTarget.FunctionArgument was recorded
 * from" by name alone (`firstOrNull { it.name == name }`) - even though the FlowTarget
 * already carries the exact [SourceLocation] the annotator observed the call at
 * (ParameterFlowAnnotator constructs it directly from `node.location`). When a callee body
 * has two calls sharing the same method name on different receivers, findCallByName always
 * returns the textually-first one, regardless of which one the recorded flow actually
 * refers to - silently following the wrong call chain (missing real evidence, or worse,
 * reporting evidence from a call the parameter never actually reached).
 */
internal class ParameterFlowQueryNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val locationA = SourceLocation("Fixture.java", 10, 1)
    private val locationB = SourceLocation("Fixture.java", 20, 1)

    private fun wrongHelper() =
        FunctionDecl(
            name = "helper",
            qualifiedName = "wrongReceiver.helper",
            parameters = listOf(Parameter("y", null)),
            declaringClass = "WrongReceiver",
            location = loc,
            children = emptyList(),
            parameterFlows = emptyList(),
        )

    private fun rightHelper() =
        FunctionDecl(
            name = "helper",
            qualifiedName = "rightReceiver.helper",
            parameters = listOf(Parameter("y", null)),
            declaringClass = "RightReceiver",
            location = loc,
            children = emptyList(),
            parameterFlows = listOf(ParameterFlow(0, "y", setOf(FlowTarget.LoopIteration(loc)))),
        )

    private fun middleCallingHelperAt(vararg calls: FunctionCall) =
        FunctionDecl(
            name = "middle",
            qualifiedName = "svc.middle",
            parameters = listOf(Parameter("data", null)),
            declaringClass = "Svc",
            location = loc,
            children = calls.toList(),
            parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.FunctionArgument("helper", locationB)))),
        )

    private fun callerInvoking(outerCall: FunctionCall) =
        FunctionDecl(
            name = "caller",
            qualifiedName = "Fixture.caller",
            parameters = listOf(Parameter("data", null)),
            declaringClass = "Fixture",
            location = loc,
            children = listOf(outerCall),
            parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.FunctionArgument("middle", loc)))),
        )

    private fun evidenceFor(
        middleFn: FunctionDecl,
        symbolTable: SymbolTable,
    ): FlowEvidence? {
        symbolTable.register(middleFn)
        val outerCall = FunctionCall("middle", "svc", emptyList(), loc, emptyList())
        return ParameterFlowQuery.parameterFlowsThrough(
            outerCall,
            callerInvoking(outerCall),
            symbolTable,
            maxDepth = 2,
        ) { it is FlowTarget.LoopIteration }
    }

    @Test
    fun `a same-named call on the wrong receiver is not mistaken for the flow-tracked one`() {
        // "wrongReceiver.helper()" comes first textually but never receives the param;
        // "rightReceiver.helper()" is the one the flow annotation actually recorded
        // (locationB) and is the only one that iterates its parameter.
        val symbolTable = SymbolTable()
        symbolTable.register(wrongHelper())
        symbolTable.register(rightHelper())
        val callAtA = FunctionCall("helper", "wrongReceiver", emptyList(), locationA, emptyList())
        val callAtB = FunctionCall("helper", "rightReceiver", emptyList(), locationB, emptyList())

        evidenceFor(middleCallingHelperAt(callAtA, callAtB), symbolTable).shouldNotBeNull()
    }

    @Test
    fun `with only the wrong-receiver call present, no evidence is found`() {
        // Regression guard for the fix itself: if the correctly-tracked call genuinely
        // isn't there, the query must not fabricate evidence from an unrelated one.
        val symbolTable = SymbolTable()
        symbolTable.register(wrongHelper())
        val callAtA = FunctionCall("helper", "wrongReceiver", emptyList(), locationA, emptyList())

        evidenceFor(middleCallingHelperAt(callAtA), symbolTable).shouldBeNull()
    }
}
