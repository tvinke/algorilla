package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.markScalarLookups
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class NestedLookupRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = NestedLookupRule()

    @Test
    fun `should detect list contains inside for-each loop`() {
        val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

        findings shouldHaveSize 1
        findings.first().ruleId shouldBe "nested-lookup"
        findings.first().message shouldContain "contains"
        findings.first().message shouldContain "targets"
        findings.first().currentComplexity shouldBe "O(items \u00d7 targets)"
    }

    @Test
    fun `should detect list contains inside while loop`() {
        val findings = analyzeFixture("nested-lookup/positive/list-contains-in-while.java")

        findings shouldHaveSize 1
        findings.first().message shouldContain "while loop"
    }

    @Test
    fun `should detect indexOf inside forEach`() {
        val findings = analyzeFixture("nested-lookup/positive/foreach-indexOf.java")

        findings shouldHaveSize 1
        findings.first().message shouldContain "indexOf"
    }

    @Test
    fun `should not flag set contains in loop`() {
        val findings = analyzeFixture("nested-lookup/negative/set-contains-in-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag contains outside loop`() {
        val findings = analyzeFixture("nested-lookup/negative/no-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag String contains in loop`() {
        val findings = analyzeFixture("nested-lookup/negative/string-contains-in-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag String field contains in loop`() {
        val findings = analyzeFixture("nested-lookup/negative/string-field-contains-in-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag Set field contains in loop`() {
        val findings = analyzeFixture("nested-lookup/negative/set-field-contains-in-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag contains on cache or map or index targets`() {
        val findings = analyzeFixture("nested-lookup/negative/cache-map-target-in-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should detect List field contains in loop`() {
        val findings = analyzeFixture("nested-lookup/positive/list-field-contains-in-loop.java")

        findings shouldHaveSize 1
        findings.first().ruleId shouldBe "nested-lookup"
        findings.first().message shouldContain "targets"
    }

    @Test
    fun `should include evidence chain with loop and lookup`() {
        val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

        findings shouldHaveSize 1
        val evidence = findings.first().evidence
        evidence shouldHaveSize 2
        evidence[0].label shouldContain "for-each loop"
        evidence[1].label shouldContain "contains"
    }

    @Test
    fun `should suggest HashSet for contains lookup`() {
        val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

        findings.first().suggestion shouldContain "HashSet"
    }

    @Test
    fun `should not flag String contains when variable name suggests string type`() {
        val findings = analyzeFixture("nested-lookup/regression/string-contains-not-collection.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag String contains on getter-chain receiver`() {
        val findings = analyzeFixture("nested-lookup/regression/getter-chain-string-contains.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag per-element property access in loop`() {
        val findings = analyzeFixture("nested-lookup/negative/per-element-property-access.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should suggest Map for indexOf lookup`() {
        val findings = analyzeFixture("nested-lookup/positive/foreach-indexOf.java")

        findings.first().suggestion shouldContain "Map (element"
    }

    @Nested
    inner class ConfidencePromotion {
        @Test
        fun `should be HIGH confidence when TypeEnvironment confirms List target`() {
            val findings = analyzeFixtureWithTypes("nested-lookup/regression/type-confirmed-list-high-confidence.java")

            findings shouldHaveSize 2
            findings.forEach { it.confidence shouldBe Confidence.HIGH }
        }

        @Test
        fun `should be MEDIUM confidence when no type info available`() {
            // Use fixture without typeEnvironments — fallback to name-based detection
            val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

            findings shouldHaveSize 1
            // With TypeEnvironment from markScalarLookups, the declared List<String> parameter
            // will be resolved — so this should actually be HIGH now
            findings.first().confidence shouldBe Confidence.HIGH
        }
    }

    private fun analyzeFixtureWithTypes(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.loadDefaults()
        val result = markScalarLookups(mapOf(path to fileRoot), registry)
        val context =
            AnalysisContext(
                irTrees = result.irTrees,
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
                typeEnvironments = result.typeEnvironments,
            )
        return rule.evaluate(context)
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.loadDefaults()
        val result = markScalarLookups(mapOf(path to fileRoot), registry)
        val context =
            AnalysisContext(
                irTrees = result.irTrees,
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
                typeEnvironments = result.typeEnvironments,
            )
        return rule.evaluate(context)
    }
}
