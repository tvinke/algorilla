package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Direct unit test for [ChainedGettersRule.buildChain], built entirely from IR nodes
 * (no parser involved) so it runs inside the `core` module — the same module as the
 * production code — and is reachable by mutation testing there.
 *
 * See `ChainedGettersRuleJavaTest` and `PrecisionRegressionTest` in `lang-java` for the
 * full-pipeline, real-Java-source version of the same regression.
 */
internal class ChainedGettersRuleTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = ChainedGettersRule()

    @Test
    fun `does not chain two independent lookups merged into one call`() {
        // loanCycleNumber = getValue(); variations = getPrincipalVariationsForBorrowerCycle();
        // fetchLoanCycleDefaultValue(variations, loanCycleNumber) is a join of two independent
        // producers, not a sequential chain (the Fineract fan-in false positive).
        val loanCycleNumberDecl = varDecl("loanCycleNumber", call("getValue"))
        val variationsDecl = varDecl("variations", call("getPrincipalVariationsForBorrowerCycle"))
        val mergeCall = call("fetchLoanCycleDefaultValue", ref("variations"), ref("loanCycleNumber"))
        val fn = functionDecl("resolveDefaultPrincipal", loanCycleNumberDecl, variationsDecl, mergeCall)

        rule.evaluate(context(fn, resolvableLookupFn("fetchLoanCycleDefaultValue"))).shouldBeEmpty()
    }

    @Test
    fun `still chains a genuine sequential dependency`() {
        // order = getOrder(id); customer = getCustomer(order) — order feeds the only
        // argument of getCustomer, a real two-link chain.
        val orderDecl = varDecl("order", call("getOrder", ref("id")))
        val getCustomerCall = call("getCustomer", ref("order"))
        val fn = functionDecl("lookup", orderDecl, getCustomerCall)

        val findings = rule.evaluate(context(fn, resolvableLookupFn("getCustomer")))

        findings shouldHaveSize 1
        // Pins the exact chain description and per-step evidence down — a regression net
        // for PIT survivors on ChainedGettersRule.buildFinding: the idx == 0 label branch
        // (would otherwise swap "starts the chain" / "uses result of previous"), the
        // chainDesc join over `it.name` (would otherwise render as "null → null"), and
        // buildFinding's own return value (a mutated null return only surfaces once
        // something downstream actually reads a field off the finding).
        val finding = findings.single()
        finding.message shouldBe "Chained getter cascade in lookup(): getOrder → getCustomer"
        finding.evidence shouldHaveSize 2
        finding.evidence[0].label shouldBe "getOrder() starts the chain"
        finding.evidence[0].depth shouldBe 0
        finding.evidence[1].label shouldBe "getCustomer() uses result of previous"
        finding.evidence[1].depth shouldBe 1
    }

    @Test
    fun `still chains when the same producer variable is referenced twice in one call`() {
        // order = getOrder(id); fetchMergedOrder(order, order) — a repeated reference to the
        // same producer is still exactly one producer, not a fan-in of two independent lookups.
        val orderDecl = varDecl("order", call("getOrder", ref("id")))
        val mergeCall = call("fetchMergedOrder", ref("order"), ref("order"))
        val fn = functionDecl("lookup", orderDecl, mergeCall)

        rule.evaluate(context(fn, resolvableLookupFn("fetchMergedOrder"))) shouldHaveSize 1
    }

    /** A resolvable [FunctionDecl] with a [LookupCall] body, so `hasLinear` finds it via the symbol table. */
    private fun resolvableLookupFn(name: String) =
        FunctionDecl(
            name = name,
            qualifiedName = "Repo.$name",
            parameters = emptyList(),
            declaringClass = "Repo",
            location = loc,
            children =
                listOf(
                    LookupCall(kind = LookupKind.FIND, targetVariable = "items", isO1 = false, location = loc, children = emptyList()),
                ),
        )

    private fun context(
        fn: FunctionDecl,
        resolvable: FunctionDecl,
    ): AnalysisContext {
        val symbolTable = SymbolTable().also { it.register(resolvable) }
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

    private fun varDecl(
        name: String,
        initializer: FunctionCall,
    ) = VariableDecl(name = name, typeName = null, initializer = initializer, location = loc, children = listOf(initializer))

    private fun call(
        name: String,
        vararg arguments: IRNode,
    ) = FunctionCall(name = name, qualifiedTarget = null, arguments = arguments.toList(), location = loc, children = emptyList())

    private fun ref(name: String) = GenericNode(nodeType = name, location = loc, children = emptyList())
}
