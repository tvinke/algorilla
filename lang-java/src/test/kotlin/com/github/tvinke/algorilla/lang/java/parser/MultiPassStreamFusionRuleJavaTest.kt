package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.markScalarLookups
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.MultiPassStreamFusionRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class MultiPassStreamFusionRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = MultiPassStreamFusionRule()

    @Nested
    inner class StreamPipelines {
        @Test
        fun `should detect two stream pipelines on same collection`() {
            val findings = analyzeFixture("multi-pass-stream-fusion/positive/two-stream-pipelines.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "multi-pass-stream-fusion"
            findings.first().message shouldContain "orders"
            findings.first().message shouldContain "streamed 2 times"
            findings.first().confidence shouldBe Confidence.MEDIUM
            findings.first().currentComplexity shouldBe "O(n \u00d7 2)"
        }

        @Test
        fun `should provide evidence for each stream call`() {
            val findings = analyzeFixture("multi-pass-stream-fusion/positive/two-stream-pipelines.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "orders"
            evidence[0].label shouldContain "1st"
            evidence[1].label shouldContain "orders"
            evidence[1].label shouldContain "#2"
        }
    }

    @Nested
    inner class ForEachLoops {
        @Test
        fun `should detect two for-each loops over same collection`() {
            val findings = analyzeFixture("multi-pass-stream-fusion/positive/two-foreach-loops.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "multi-pass-stream-fusion"
            findings.first().message shouldContain "items"
            findings.first().message shouldContain "iterated 2 times"
            findings.first().confidence shouldBe Confidence.LOW
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag streams on different collections`() {
            val findings = analyzeFixture("multi-pass-stream-fusion/negative/different-sources.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag streams in mutually exclusive branches`() {
            val findings = analyzeFixture("multi-pass-stream-fusion/negative/branched-streams.java")

            findings.shouldBeEmpty()
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.loadDefaults()
        val irTrees = markScalarLookups(mapOf(path to fileRoot), registry)
        val context =
            AnalysisContext(
                irTrees = irTrees,
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
