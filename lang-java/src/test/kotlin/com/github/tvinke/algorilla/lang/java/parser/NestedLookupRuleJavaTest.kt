package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
    fun `should include evidence chain with loop and lookup`() {
        val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

        findings shouldHaveSize 1
        val evidence = findings.first().evidence
        evidence shouldHaveSize 2
        evidence[0].label shouldContain "for-each loop"
        evidence[1].label shouldContain "contains"
    }

    @Test
    fun `should suggest HashSet or Map`() {
        val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.java")

        findings.first().suggestion shouldContain "HashSet/Map"
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
