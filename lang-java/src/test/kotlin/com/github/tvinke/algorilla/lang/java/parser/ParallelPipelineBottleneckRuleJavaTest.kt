package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.ParallelPipelineBottleneckRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class ParallelPipelineBottleneckRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = ParallelPipelineBottleneckRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect parallelStream forEach with shared mutation`() {
            val findings = analyzeFixture("parallel-pipeline-bottleneck/positive/parallel-foreach-shared-add.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "parallel-pipeline-bottleneck"
            findings.first().message shouldContain "results"
            findings.first().message shouldContain "add"
            findings.first().message shouldContain "parallelStream"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag parallelStream with collect`() {
            val findings = analyzeFixture("parallel-pipeline-bottleneck/negative/parallel-collect-safe.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag sequential stream with shared mutation`() {
            val findings = analyzeFixture("parallel-pipeline-bottleneck/negative/sequential-stream-add.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndMetadata {
        @Test
        fun `should include evidence with parallel loop and mutation`() {
            val findings = analyzeFixture("parallel-pipeline-bottleneck/positive/parallel-foreach-shared-add.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "parallelStream"
            evidence[1].label shouldContain "add"
        }

        @Test
        fun `should report category as concurrency`() {
            rule.category.displayName shouldBe "Concurrency"
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)

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
