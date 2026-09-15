package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Regression coverage for #137: [NPlusOneRepositoryCallRule.checkCallInLoop] used to call the
 * plain [com.github.tvinke.algorilla.util.CrossMethodResolver.resolveAndFind] to find the hidden
 * fetch, discarding the [com.github.tvinke.algorilla.util.ResolutionConfidence] of the resolution
 * chain entirely - so a hidden fetch reached only through an ambiguous overload guess whose
 * target also matches a repository naming pattern still reported HIGH confidence. Same treatment
 * as [HiddenNestedLoopRuleTest]/[NestedLookupRuleTest]/[IOInLoopRuleTest].
 */
internal class NPlusOneRepositoryCallRuleConfidenceTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = NPlusOneRepositoryCallRule()

    @Test
    fun `demotes cross-method N+1 finding to LOW when the callee resolves via an ambiguous overload guess`() {
        val call = FunctionCall(name = "helper", qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val caller = functionDecl("process", loop)

        val symbolTable =
            SymbolTable().also {
                it.register(caller)
                registerAmbiguousHelperCandidates(it)
            }

        val findings = rule.evaluate(context(caller, symbolTable))

        findings shouldHaveSize 1
        // Before the fix: resolveAndFind discarded the AMBIGUOUS_OVERLOAD_BEST_GUESS confidence,
        // so a repo-pattern-matching target (userRepository) drove this straight to HIGH
        // regardless of the arbitrary pick between two same-arity "helper" candidates.
        findings.single().confidence shouldBe Confidence.LOW
    }

    /**
     * Registers two same-name, same-arity "helper" candidates - registration order matters:
     * bestMatch's arbitrary guess among them is [List.first], so `helperWithFetch` (the one
     * that actually contains the single-record fetch) must go first for the finding to fire.
     */
    private fun registerAmbiguousHelperCandidates(symbolTable: SymbolTable) {
        val fetch =
            FunctionCall(
                name = "findByEmail",
                qualifiedTarget = "userRepository",
                arguments = emptyList(),
                location = loc,
                children = emptyList(),
            )
        val helperWithFetch = functionDecl("helper", fetch)
        val helperWithoutFetch = functionDecl("helper")
        symbolTable.register(helperWithFetch)
        symbolTable.register(helperWithoutFetch)
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
}
