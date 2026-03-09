package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSortComparatorRule
import com.github.tvinke.algorilla.rules.builtin.FilterAfterSortRule
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
            findings.first().ruleId shouldBe "expensive-construction"
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
            findings.first().currentComplexity shouldBe "O(n \u00d7 2)"
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
    inner class ExpensiveSortComparatorTests {
        private val rule = ExpensiveSortComparatorRule()

        @Test
        fun `should detect lookup inside sort comparator`() {
            val findings = analyzeFixture("expensive-sort-comparator/positive/lookup-in-comparator.java", rule)

            findings.size shouldBe 2
            findings.all { it.ruleId == "expensive-sort-comparator" } shouldBe true
            findings.first().currentComplexity shouldBe "O(n\u00b2 log n)"
            findings.first().suggestedComplexity shouldBe "O(n log n)"
        }

        @Test
        fun `should not flag simple comparator`() {
            val findings = analyzeFixture("expensive-sort-comparator/negative/simple-comparator.java", rule)

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class DateInSortTests {
        private val rule = ExpensiveSortComparatorRule()

        @Test
        fun `should detect Date creation inside sort comparator`() {
            val findings = analyzeFixture("date-in-sort/positive/date-creation-in-comparator.java", rule)

            findings.size shouldBe 2
            findings.all { it.ruleId == "expensive-sort-comparator" } shouldBe true
            findings.first().message shouldContain "Date"
        }

        @Test
        fun `should not flag comparator without date creation`() {
            val findings = analyzeFixture("date-in-sort/negative/no-date-in-comparator.java", rule)

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class FilterAfterSortTests {
        private val rule = FilterAfterSortRule()

        @Test
        fun `should detect filter after sort in stream pipeline`() {
            val findings = analyzeFixture("filter-after-sort/positive/sort-then-filter.java", rule)

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "filter-after-sort"
            findings.first().message shouldContain "filter"
            findings.first().message shouldContain "sorted"
        }

        @Test
        fun `should not flag correct filter-then-sort order`() {
            val findings = analyzeFixture("filter-after-sort/negative/filter-then-sort.java", rule)

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag separate stream chains`() {
            val findings = analyzeFixture("filter-after-sort/negative/separate-chains.java", rule)

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag sort and filter in separate chains with if block`() {
            val findings = analyzeFixture("filter-after-sort/negative/separate-chains-with-if.java", rule)

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
            findings.first().ruleId shouldBe "bulk-load-for-single-lookup"
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
