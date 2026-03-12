package com.github.tvinke.algorilla.lang.groovy.parser

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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class HiddenNestedLoopRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = HiddenNestedLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect loop inside method called from outer each loop`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/each-calls-method-with-loop.groovy")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "hidden-nested-loop"
            findings.first().message shouldContain "validateItems()"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag method without inner loop`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/each-calls-method-without-loop.groovy")

            findings.shouldBeEmpty()
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val symbolTable = SymbolTable()
        fileRoot.findDescendants<FunctionDecl>().forEach { fn -> symbolTable.register(fn) }
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
