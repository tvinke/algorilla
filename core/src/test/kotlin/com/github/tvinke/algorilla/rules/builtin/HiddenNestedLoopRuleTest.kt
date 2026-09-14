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
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Direct unit test for [HiddenNestedLoopRule]'s cross-method confidence handling, built
 * entirely from IR nodes (no parser involved) - see [ChainedGettersRuleTest] for the
 * established pattern this follows.
 *
 * Regression coverage for a gap an architecture review found: `checkForHiddenLoop` resolves
 * `call` itself once, directly, to get [com.github.tvinke.algorilla.util.ResolutionConfidence]
 * - but the flow-confirmation evidence from [ParameterFlowQuery.parameterFlowsThrough] can
 * cross its *own* ambiguous overload guess one or more hops into the flow chain, entirely
 * independent of whether `call` itself resolved cleanly. Before the fix, that inner ambiguity
 * never reached [com.github.tvinke.algorilla.util.demoteIfAmbiguous], so a finding could
 * report HIGH confidence ("flow-confirmed") built on top of a guess.
 */
internal class HiddenNestedLoopRuleTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = HiddenNestedLoopRule()

    @Test
    fun `demotes to LOW when flow-confirmation crosses an ambiguous hop, even though the call itself resolves exactly`() {
        val (caller, symbolTable) = ambiguousFlowHopFixture()

        val findings = rule.evaluate(context(caller, symbolTable))

        findings shouldHaveSize 1
        // Before the fix: resolutionConfidence came only from resolving "processAll" itself
        // (EXACT), so flowConfirmed=true drove this straight to HIGH regardless of the
        // ambiguous "innerStep" hop the flow evidence actually went through.
        findings.single().confidence shouldBe Confidence.LOW
    }

    /**
     * for (item in items) { processAll() } - "processAll" is the ONE registered candidate
     * (exact). Its own body hides a loop (the finding) and calls "innerStep()", which has TWO
     * same-name, same-arity candidates registered (ambiguous). Only the first-registered one
     * (innerStepWithFlow) actually reports LoopIteration - bestMatch's arbitrary pick has to
     * land there for flow-confirmation to succeed at all.
     */
    private fun ambiguousFlowHopFixture(): Pair<FunctionDecl, SymbolTable> {
        val outerLoop = LoopNode(kind = LoopKind.FOR, iteratedVariable = "items", location = loc, children = listOf(call("processAll")))
        val caller =
            functionDecl("caller", outerLoop).also {
                it.parameterFlows = listOf(ParameterFlow(0, "items", setOf(FlowTarget.FunctionArgument("processAll", loc))))
            }
        val processAllFn =
            functionDecl("processAll", hiddenLoop(), call("innerStep")).also {
                it.parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.FunctionArgument("innerStep", loc))))
            }

        val symbolTable =
            SymbolTable().also {
                it.register(caller)
                it.register(processAllFn)
                registerAmbiguousInnerStepCandidates(it)
            }
        return caller to symbolTable
    }

    private fun hiddenLoop() =
        LoopNode(
            kind = LoopKind.FOR,
            iteratedVariable = "data",
            location = loc,
            children =
                listOf(
                    LookupCall(kind = LookupKind.FIND, targetVariable = "x", isO1 = false, location = loc, children = emptyList()),
                ),
        )

    /**
     * Registers two same-name, same-arity "innerStep" candidates - registration order
     * matters: bestMatch's arbitrary guess among them is [List.first] over however they were
     * registered, so `innerStepWithFlow` must go first for flow-confirmation to succeed.
     */
    private fun registerAmbiguousInnerStepCandidates(symbolTable: SymbolTable) {
        val innerStepWithFlow =
            functionDecl("innerStep").also {
                it.parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.LoopIteration(loc))))
            }
        val innerStepWithoutFlow = functionDecl("innerStep")
        symbolTable.register(innerStepWithFlow)
        symbolTable.register(innerStepWithoutFlow)
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
