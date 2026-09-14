package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isPartitionedIteration's
 * job is to tell "the inner loop iterates a per-element child collection of the outer
 * loop" (O(sum), suppress) apart from "two genuinely unrelated collections" (O(product),
 * keep flagging). Its de-pluralize-and-prefix-match check (`innerBase.startsWith(outerClean,
 * ignoreCase = true)`) had no word-boundary: "cars" -> outerClean "car" matches any inner
 * base starting with "car" — "career", "cargo", "carton" — none of which have anything to
 * do with individual cars. A genuine Cartesian-product bug over `cars` x `career` would be
 * silently suppressed. Same shape as the camelCase-prefix bugs elsewhere in this campaign,
 * just applied to a de-pluralized element name instead of a method name.
 */
internal class CardinalityExplosionRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = CardinalityExplosionRule()
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    private data class UnrelatedPrefixCase(
        val outerVar: String,
        val innerVar: String,
    )

    private val unrelatedPrefixCollisions =
        listOf(
            UnrelatedPrefixCase("cars", "career.getSomething()"),
            UnrelatedPrefixCase("items", "itemizedList.getStuff()"),
            UnrelatedPrefixCase("cats", "category.getName()"),
        )

    @Test
    fun `an unrelated variable sharing only a text prefix with the outer element is still a Cartesian product`() {
        unrelatedPrefixCollisions.forEach { case ->
            nestedLoopFindings(case.outerVar, case.innerVar) shouldHaveSize 1
        }
    }

    @Test
    fun `an exact singular-plural element match is still recognized as partitioned iteration`() {
        // Regression guard matching the existing parent-child-iteration.java fixture:
        // departments -> department.getEmployees() must stay suppressed.
        nestedLoopFindings("departments", "department.getEmployees()").shouldBeEmpty()
    }

    @Test
    fun `a compound element name at a real word boundary is still recognized as partitioned iteration`() {
        // departmentHead is still "a department", just a more specific per-element name -
        // the boundary check (uppercase right after the shared prefix) must allow this,
        // not just the exact-match case.
        nestedLoopFindings("departments", "departmentHead.getStaff()").shouldBeEmpty()
    }

    /**
     * Documented finding (cc #73), not fixed here: classifyMutation treats any single-letter
     * receiver name calling add() as a scalar accumulator (BigDecimal-style), never a
     * collection. A short-named List (`l.add(x)`, `r.add(x)`) in a genuine Cartesian nesting
     * is silently reclassified as SCALAR_ACCUMULATION and the whole group gets suppressed.
     * This can't be fixed locally without wiring a TypeEnvironment through this rule (it
     * currently has none) — closer to a V3-light "extend existing infra" step than an
     * in-file tweak, so it's pinned here rather than changed.
     */
    @Test
    fun `a single-letter collection receiver is still misread as a scalar accumulator (documented gap)`() {
        nestedLoopFindings("orders", "products", mutationTarget = "l").shouldBeEmpty()
    }

    /**
     * Documented finding (cc #73), not fixed here: scanFlatMap matches the bare method name
     * "flatMap" with no registry lookup and no receiver-type confirmation — the one hardcoded
     * name check in this file (the others already consult YAML-driven sets). A class with
     * its own unrelated flatMap() method would collide the same way a real Stream one does.
     * Fixing this properly means adding a language-aware flatmap-equivalent-methods YAML
     * section (Groovy's collectMany, other frameworks' operators) - real research, not a
     * quick local tweak, so left as a finding.
     */
    @Test
    fun `flatMap is matched by bare method name regardless of receiver (documented gap)`() {
        val innerCall = FunctionCall("stream", "other", emptyList(), loc, emptyList())
        val flatMapCall =
            FunctionCall(
                "flatMap",
                "unrelatedBuilder",
                listOf(GenericNode("lambda", loc, listOf(innerCall))),
                loc,
                listOf(GenericNode("lambda", loc, listOf(innerCall))),
            )
        rule.evaluate(fixtureContext(listOf(flatMapCall))) shouldHaveSize 1
    }

    private fun nestedLoopFindings(
        outerVar: String,
        innerVar: String,
        mutationTarget: String = "resultList",
    ): List<Finding> {
        val mutationCall =
            FunctionCall("add", mutationTarget, listOf(GenericNode("x", loc, emptyList())), loc, emptyList())
        val innerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = innerVar, location = loc, children = listOf(mutationCall))
        val outerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = outerVar, location = loc, children = listOf(innerLoop))
        return rule.evaluate(fixtureContext(listOf(outerLoop)))
    }

    private fun fixtureContext(children: List<com.github.tvinke.algorilla.model.IRNode>): AnalysisContext {
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = children)
        return AnalysisContext(
            irTrees = mapOf("Fixture.java" to fileRoot),
            symbolTable = SymbolTable(),
            callGraph = CallGraph(),
            config = AnalysisConfig(),
            registry = registry,
        )
    }
}
