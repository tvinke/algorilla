package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RegexRecompilationInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RegexRecompilationInLoopRuleJsTest {
    private val parser = JavaScriptLanguageParser()
    private val rule = RegexRecompilationInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect regex literal arguments in loop`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/positive/regex-in-loop.js")

            findings shouldHaveSize 3
            findings.all { it.ruleId == "regex-recompilation-in-loop" } shouldBe true
            findings.any { it.message.contains("replace") } shouldBe true
            findings.any { it.message.contains("match") } shouldBe true
            findings.any { it.message.contains("split") } shouldBe true
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag split and replace with string literal arguments`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/negative/string-args-in-loop.js")

            findings.shouldBeEmpty()
        }
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
