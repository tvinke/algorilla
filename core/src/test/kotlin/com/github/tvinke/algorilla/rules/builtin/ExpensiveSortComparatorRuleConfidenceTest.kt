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
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Regression coverage for #137: [ExpensiveSortComparatorRule.checkCrossMethodDate] used to call
 * the plain [com.github.tvinke.algorilla.util.CrossMethodResolver.resolveAndFind], discarding
 * the [com.github.tvinke.algorilla.util.ResolutionConfidence] of the resolution chain entirely.
 * This rule's [ExpensiveSortComparatorRule.defaultConfidence] is HIGH, and
 * [com.github.tvinke.algorilla.engine.FindingPostProcessor] baselines any MEDIUM finding up to
 * that ceiling - so an ambiguous overload guess used to make it all the way to a HIGH-confidence
 * finding. Same treatment as [HiddenNestedLoopRuleTest]/[NestedLookupRuleTest]/[IOInLoopRuleTest].
 */
internal class ExpensiveSortComparatorRuleConfidenceTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = ExpensiveSortComparatorRule()

    @Test
    fun `demotes cross-method date-creation finding to LOW when the callee resolves via an ambiguous overload guess`() {
        val call = FunctionCall(name = "helper", qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
        val sort =
            SortCall(
                kind = SortKind.SORT,
                comparatorBody = listOf(call),
                location = loc,
                children = emptyList(),
            )
        val caller = functionDecl("process", sort)

        val symbolTable =
            SymbolTable().also {
                it.register(caller)
                registerAmbiguousHelperCandidates(it)
            }

        val findings = rule.evaluate(context(caller, symbolTable))

        findings shouldHaveSize 1
        // Before the fix: resolveAndFind discarded the AMBIGUOUS_OVERLOAD_BEST_GUESS confidence,
        // so this reported HIGH (this rule's defaultConfidence ceiling) as if "helperWithDate"
        // were a sure match rather than an arbitrary pick among two same-arity candidates.
        findings.single().confidence shouldBe Confidence.LOW
    }

    /**
     * Registers two same-name, same-arity "helper" candidates - registration order matters:
     * bestMatch's arbitrary guess among them is [List.first], so `helperWithDate` (the one that
     * actually contains the expensive operation) must go first for the finding to fire at all.
     */
    private fun registerAmbiguousHelperCandidates(symbolTable: SymbolTable) {
        val dateCreation = ObjectCreation(typeName = "Date", location = loc, children = emptyList())
        val helperWithDate = functionDecl("helper", dateCreation)
        val helperWithoutDate = functionDecl("helper")
        symbolTable.register(helperWithDate)
        symbolTable.register(helperWithoutDate)
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
