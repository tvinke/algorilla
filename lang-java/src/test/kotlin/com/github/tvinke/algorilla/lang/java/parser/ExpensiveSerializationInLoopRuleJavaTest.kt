package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSerializationInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class ExpensiveSerializationInLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = ExpensiveSerializationInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect writeValueAsString inside order export loop`() {
            val findings = analyzeFixture("expensive-serialization-in-loop/positive/write-value-in-order-export.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "expensive-serialization-in-loop"
            findings.first().message shouldContain "writeValueAsString"
        }

        @Test
        fun `should detect readValue inside config loading loop`() {
            val findings = analyzeFixture("expensive-serialization-in-loop/positive/read-value-in-config-loader.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "readValue"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag serialization called outside loop`() {
            val findings = analyzeFixture("expensive-serialization-in-loop/negative/serialization-outside-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag non-serialization methods in loop`() {
            val findings = analyzeFixture("expensive-serialization-in-loop/negative/non-serialization-in-loop.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include loop and serialization bottleneck evidence`() {
            val findings = analyzeFixture("expensive-serialization-in-loop/positive/write-value-in-order-export.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "for-each"
            evidence[1].label shouldContain "writeValueAsString"
            evidence[1].complexity shouldContain "bottleneck"
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
