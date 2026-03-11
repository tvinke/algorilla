package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.FullScanForSingleLookupRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class FullScanForSingleLookupRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = FullScanForSingleLookupRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect findAll followed by filter`() {
            val findings = analyzeFixture("full-scan-for-single-lookup/positive/find-all-then-filter.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "bulk-load-for-single-lookup"
            findings.first().message shouldContain "findAll"
            findings.first().currentComplexity shouldBe "O(n)"
            findings.first().suggestedComplexity shouldBe "O(1)"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag method without bulk load`() {
            val findings = analyzeFixture("full-scan-for-single-lookup/negative/no-bulk-load.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence chain for findAll then filter`() {
            val findings = analyzeFixture("full-scan-for-single-lookup/positive/find-all-then-filter.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "findAll"
            evidence[0].label shouldContain "loads all"
            evidence[0].complexity shouldContain "O(n)"
            evidence[1].label shouldContain "filtering"
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
