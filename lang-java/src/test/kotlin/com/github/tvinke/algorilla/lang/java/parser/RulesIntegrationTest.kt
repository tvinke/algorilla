package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.builtin.FullScanForSingleLookupRule
import com.github.tvinke.algorilla.rules.builtin.HeavyweightObjectPerInvocationRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedLinearScanRule
import com.github.tvinke.algorilla.rules.builtin.SortForLastRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RulesIntegrationTest {
    private val parser = JavaLanguageParser()

    @Nested
    inner class SortForLastTests {
        private val rule = SortForLastRule()

        @Test
        fun `should detect sort followed by findFirst`() {
            val findings = analyzeFixture("sort-for-last/positive/sort-then-first.java", rule)

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "sort-for-last"
            findings.first().currentComplexity shouldBe "O(n log n)"
            findings.first().suggestedComplexity shouldBe "O(n)"
        }

        @Test
        fun `should not flag sort without first or last access`() {
            val findings = analyzeFixture("sort-for-last/negative/sort-then-iterate.java", rule)

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class HeavyweightObjectTests {
        private val rule = HeavyweightObjectPerInvocationRule()

        @Test
        fun `should detect ObjectMapper in method body`() {
            val findings =
                analyzeFixture(
                    "heavyweight-object-per-invocation/positive/mapper-in-method.java",
                    rule,
                )

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "heavyweight-object-per-invocation"
            findings.first().message shouldContain "ObjectMapper"
            findings.first().message shouldContain "serialize"
        }

        @Test
        fun `should detect Gson in method body`() {
            val findings =
                analyzeFixture(
                    "heavyweight-object-per-invocation/positive/gson-in-method.java",
                    rule,
                )

            findings shouldHaveSize 1
            findings.first().message shouldContain "Gson"
        }

        @Test
        fun `should not flag non-heavyweight object creation`() {
            val findings =
                analyzeFixture(
                    "heavyweight-object-per-invocation/negative/no-heavyweight-types.java",
                    rule,
                )

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class RepeatedLinearScanTests {
        private val rule = RepeatedLinearScanRule()

        @Test
        fun `should detect multiple filters on same collection`() {
            val findings =
                analyzeFixture(
                    "repeated-linear-scan/positive/multiple-filters.java",
                    rule,
                )

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "repeated-linear-scan"
            findings.first().message shouldContain "users"
            findings.first().currentComplexity shouldBe "O(n*k)"
        }

        @Test
        fun `should not flag single lookup`() {
            val findings =
                analyzeFixture(
                    "repeated-linear-scan/negative/single-lookup.java",
                    rule,
                )

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class FullScanForSingleLookupTests {
        private val rule = FullScanForSingleLookupRule()

        @Test
        fun `should detect findAll followed by filter`() {
            val findings =
                analyzeFixture(
                    "full-scan-for-single-lookup/positive/find-all-then-filter.java",
                    rule,
                )

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "full-scan-for-single-lookup"
            findings.first().message shouldContain "findAll"
            findings.first().currentComplexity shouldBe "O(n)"
            findings.first().suggestedComplexity shouldBe "O(1)"
        }

        @Test
        fun `should not flag method without bulk load`() {
            val findings =
                analyzeFixture(
                    "full-scan-for-single-lookup/negative/no-bulk-load.java",
                    rule,
                )

            findings.shouldBeEmpty()
        }
    }

    private fun analyzeFixture(
        fixturePath: String,
        rule: Rule,
    ): List<Finding> {
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
