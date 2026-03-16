package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.ParallelPipelineBottleneckRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class ParallelPipelineBottleneckRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = ParallelPipelineBottleneckRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect parallelStream forEach with shared mutation`() {
            val findings = analyzeFixture("parallel-pipeline-bottleneck/positive/shared-mutation.groovy")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "parallel-pipeline-bottleneck"
            findings.first().message shouldContain "add"
            findings.first().message shouldContain "parallelStream"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag sequential stream with shared mutation`() {
            val findings = analyzeFixture("parallel-pipeline-bottleneck/negative/sequential-stream.groovy")

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
        val symbolTable = SymbolTable()
        fileRoot.findDescendants<FunctionDecl>().forEach { fn -> symbolTable.register(fn) }
        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
