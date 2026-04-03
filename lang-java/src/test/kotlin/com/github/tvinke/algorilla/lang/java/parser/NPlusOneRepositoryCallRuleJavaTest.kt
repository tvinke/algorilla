package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.NPlusOneRepositoryCallRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class NPlusOneRepositoryCallRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = NPlusOneRepositoryCallRule()

    @Nested
    inner class WidenedPattern {
        @Test
        fun `should detect findByEmail in loop`() {
            val findings = analyzeFixture("n-plus-one-query/positive/wide-pattern-match.java")

            findings shouldHaveSize 3
            val messages = findings.map { it.message }
            messages.any { it.contains("findByEmail") } shouldBe true
            messages.any { it.contains("getByTrackingNumber") } shouldBe true
            messages.any { it.contains("getBySku") } shouldBe true
        }
    }

    @Nested
    inner class BatchPatterns {
        @Test
        fun `should not flag batch patterns`() {
            val findings = analyzeFixture("n-plus-one-query/negative/batch-patterns-excluded.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class PaginatedBatch {
        @Test
        fun `should not flag Spring Data findFirst-N-By or findTop-N-By as N+1`() {
            val findings = analyzeFixture("n-plus-one-query/negative/paginated-batch-excluded.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class CacheExclusion {
        @Test
        fun `should not flag cache or memo or pool targets`() {
            val findings = analyzeFixture("n-plus-one-query/negative/cache-target-excluded.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class ConfidencePromotion {
        @Test
        fun `should be HIGH confidence when target matches repository pattern`() {
            val findings = analyzeFixture("n-plus-one-query/regression/repo-target-high-confidence.java")

            findings shouldHaveSize 2
            // userRepository matches 'repository' in io-target-patterns
            val repoFinding = findings.first { it.message.contains("userRepository") }
            repoFinding.confidence shouldBe Confidence.HIGH
        }

        @Test
        fun `should be HIGH confidence for wide-pattern repo targets`() {
            val findings = analyzeFixture("n-plus-one-query/positive/wide-pattern-match.java")

            findings shouldHaveSize 3
            // orderRepository and productRepository match io-target-patterns
            findings.filter { it.confidence == Confidence.HIGH }.size shouldBe 3
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)

        // Build symbol table so CrossMethodResolver can resolve calls
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
