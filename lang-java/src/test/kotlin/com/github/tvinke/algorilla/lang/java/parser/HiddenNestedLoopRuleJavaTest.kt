package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.HiddenNestedLoopRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.io.File

internal class HiddenNestedLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = HiddenNestedLoopRule()

    @Test
    fun `should detect loop inside method called from for-each loop`() {
        val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

        findings shouldHaveSize 1
        findings.first().ruleId shouldBe "hidden-nested-loop"
        findings.first().message shouldContain "validateItems()"
        findings.first().message shouldContain "hidden"
    }

    @Test
    fun `should detect loop inside method called from forEach`() {
        val findings = analyzeFixture("hidden-nested-loop/positive/foreach-calls-method-with-loop.java")

        findings shouldHaveSize 1
        findings.first().message shouldContain "summarize()"
    }

    @Test
    fun `should include evidence with outer loop and hidden inner loop`() {
        val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

        findings shouldHaveSize 1
        val evidence = findings.first().evidence
        evidence shouldHaveSize 3
        evidence[0].label shouldContain "for-each loop"
        evidence[1].label shouldContain "validateItems()"
        evidence[2].label shouldContain "for-each loop"
        evidence[2].label shouldContain "validateItems()"
    }

    @Test
    fun `should show complexity with variable names`() {
        val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

        findings shouldHaveSize 1
        findings.first().currentComplexity shouldContain "orders"
    }

    @Test
    fun `should not flag method without inner loop`() {
        val findings = analyzeFixture("hidden-nested-loop/negative/loop-calls-method-without-loop.java")

        findings.shouldBeEmpty()
    }

    @Test
    fun `should not flag method with loop called outside loop context`() {
        val findings = analyzeFixture("hidden-nested-loop/negative/no-loop-context.java")

        findings.shouldBeEmpty()
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)

        // Build symbol table so CrossMethodResolver can resolve calls
        val symbolTable = SymbolTable()
        fileRoot.findDescendants<FunctionDecl>().forEach { fn ->
            symbolTable.register(fn)
        }

        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
