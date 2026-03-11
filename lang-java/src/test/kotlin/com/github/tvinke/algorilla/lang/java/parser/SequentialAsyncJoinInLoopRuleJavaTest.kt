package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.SequentialAsyncJoinInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class SequentialAsyncJoinInLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = SequentialAsyncJoinInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect future join inside order processing loop`() {
            val findings = analyzeFixture("sequential-async-join-in-loop/positive/future-join-in-order-processing.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "sequential-async-join-in-loop"
            findings.first().message shouldContain "join"
            findings.first().message shouldContain "future"
        }

        @Test
        fun `should detect completable future join in batch processing loop`() {
            val findings = analyzeFixture("sequential-async-join-in-loop/positive/completable-future-get-in-batch.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "join"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag futures collected then joined outside loop`() {
            val findings = analyzeFixture("sequential-async-join-in-loop/negative/futures-collected-then-joined.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag join on non-future variable`() {
            val findings = analyzeFixture("sequential-async-join-in-loop/negative/join-on-non-future-variable.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include loop and blocking wait evidence`() {
            val findings = analyzeFixture("sequential-async-join-in-loop/positive/future-join-in-order-processing.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "for-each"
            evidence[1].label shouldContain "join"
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
