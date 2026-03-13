package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.UnmemoizedRecursionRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class UnmemoizedRecursionRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = UnmemoizedRecursionRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect naive fibonacci without memoization`() {
            val findings = analyzeFixture("unmemoized-recursion/positive/fibonacci.groovy")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "unmemoized-recursion"
            findings.first().message shouldContain "fib"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag tree traversal with child accessors`() {
            val findings = analyzeFixture("unmemoized-recursion/negative/tree-traversal.groovy")

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
