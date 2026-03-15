package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RedundantExpensiveCallRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RedundantExpensiveCallRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = RedundantExpensiveCallRule()

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag replaceAll calls with different string arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/different-string-args.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag disable calls with different enum arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/different-enum-args.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag calls with different method call arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/different-method-call-args.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag real-world replaceAll with different regex patterns`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/real-world-replaceall.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag side-effect methods like disable and enable`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/side-effect-disable.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag calls with different variable arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/different-variable-args.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag decode with sequential nextToken arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/decode-next-token.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class PositiveCases {
        @Test
        fun `should flag calls with identical method call arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/positive/same-method-call-args.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "redundant-expensive-call"
            findings.first().message shouldContain "findByName"
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
