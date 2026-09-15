package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Direct unit test for [IOInLoopRule]'s cross-method confidence handling, built entirely from
 * IR nodes (no parser involved) - see [ChainedGettersRuleTest] for the established pattern.
 *
 * Regression coverage for a gap a code-review pass found: `checkCrossMethodIO` reads
 * [com.github.tvinke.algorilla.util.FlowEvidence.paramName] from
 * [com.github.tvinke.algorilla.util.ParameterFlowQuery.parameterFlowsThrough] but never read
 * the (then-new) `resolutionConfidence` field, so `buildCrossMethodFinding` silently fell back
 * to `Finding`'s default `Confidence.MEDIUM` regardless of whether the flow chain that found
 * the IO call crossed an ambiguous overload guess - the same shape of bug already fixed in
 * `HiddenNestedLoopRule` and `NestedLookupRule`, left open in this third consumer of the same
 * `FlowEvidence` API.
 */
internal class IOInLoopRuleTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = IOInLoopRule()

    @Test
    fun `demotes to LOW when the IO flow crosses an ambiguous hop, even though the call itself resolves exactly`() {
        val (caller, symbolTable) = ambiguousIOFlowFixture()

        val findings = rule.evaluate(context(caller, symbolTable))

        findings shouldHaveSize 1
        // Before the fix: buildCrossMethodFinding never received a confidence at all and
        // Finding's constructor default (MEDIUM) silently won, regardless of the ambiguous
        // "innerStep" hop the flow evidence actually went through.
        findings.single().confidence shouldBe Confidence.LOW
    }

    /**
     * for (item in items) { helper() } - "helper" is the ONE registered candidate (exact). Its
     * own body calls "innerStep()", which has TWO same-name, same-arity candidates registered
     * (ambiguous). Only the first-registered one (innerStepWithIO) actually reports an
     * executeQuery MethodCallReceiver - bestMatch's arbitrary pick has to land there for the
     * flow to be found at all.
     */
    private fun ambiguousIOFlowFixture(): Pair<FunctionDecl, SymbolTable> {
        val outerLoop = LoopNode(kind = LoopKind.FOR, iteratedVariable = "items", location = loc, children = listOf(call("helper")))
        val caller =
            functionDecl("caller", outerLoop).also {
                it.parameterFlows = listOf(ParameterFlow(0, "items", setOf(FlowTarget.FunctionArgument("helper", loc))))
            }
        val helperFn =
            functionDecl("helper", call("innerStep")).also {
                it.parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.FunctionArgument("innerStep", loc))))
            }

        val symbolTable =
            SymbolTable().also {
                it.register(caller)
                it.register(helperFn)
                registerAmbiguousInnerStepCandidates(it)
            }
        return caller to symbolTable
    }

    /**
     * Registers two same-name, same-arity "innerStep" candidates - registration order matters:
     * bestMatch's arbitrary guess among them is [List.first] over however they were
     * registered, so `innerStepWithIO` must go first for the flow to be found at all.
     */
    private fun registerAmbiguousInnerStepCandidates(symbolTable: SymbolTable) {
        val innerStepWithIO =
            functionDecl("innerStep").also {
                it.parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.MethodCallReceiver("executeQuery", loc))))
            }
        val innerStepWithoutIO = functionDecl("innerStep")
        symbolTable.register(innerStepWithIO)
        symbolTable.register(innerStepWithoutIO)
    }

    private fun context(
        fn: FunctionDecl,
        symbolTable: SymbolTable,
    ): AnalysisContext {
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        return AnalysisContext(
            irTrees = mapOf("Fixture.java" to fileRoot),
            symbolTable = symbolTable,
            callGraph = CallGraph(),
            config = AnalysisConfig(),
        )
    }

    private fun functionDecl(
        name: String,
        vararg body: IRNode,
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Fixture.$name",
        parameters = emptyList(),
        declaringClass = "Fixture",
        location = loc,
        children = body.toList(),
    )

    private fun call(name: String) =
        FunctionCall(name = name, qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
}
