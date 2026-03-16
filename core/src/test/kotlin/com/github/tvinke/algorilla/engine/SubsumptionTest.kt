package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SubsumptionTest {
    private val file = "/src/OrderService.java"

    @Nested
    inner class BasicSubsumption {
        @Test
        fun `subsumed finding is dropped when dominant rule fires at same location`() {
            val dominant = finding("nested-lookup", line = 42)
            val subsumed = finding("expensive-callback", line = 42)
            val rules =
                listOf(
                    stubRule("nested-lookup", subsumes = setOf("expensive-callback")),
                    stubRule("expensive-callback"),
                )

            val result = applySubsumption(listOf(dominant, subsumed), rules)

            result shouldHaveSize 1
            result.first().ruleId shouldBe "nested-lookup"
        }

        @Test
        fun `subsumed finding survives when dominant rule does not fire at that location`() {
            val dominant = finding("nested-lookup", line = 10)
            val subsumed = finding("expensive-callback", line = 42)
            val rules =
                listOf(
                    stubRule("nested-lookup", subsumes = setOf("expensive-callback")),
                    stubRule("expensive-callback"),
                )

            val result = applySubsumption(listOf(dominant, subsumed), rules)

            result shouldHaveSize 2
        }

        @Test
        fun `findings from unrelated rules at same location both survive`() {
            val a = finding("nested-lookup", line = 42)
            val b = finding("string-concat-in-loop", line = 42)
            val rules =
                listOf(
                    stubRule("nested-lookup", subsumes = setOf("expensive-callback")),
                    stubRule("string-concat-in-loop"),
                )

            val result = applySubsumption(listOf(a, b), rules)

            result shouldHaveSize 2
        }
    }

    @Nested
    inner class TransitiveAndMultiple {
        @Test
        fun `rule can subsume multiple other rules`() {
            val dominant = finding("nested-lookup", line = 42)
            val sub1 = finding("expensive-callback", line = 42)
            val sub2 = finding("repeated-linear-scan", line = 42)
            val rules =
                listOf(
                    stubRule("nested-lookup", subsumes = setOf("expensive-callback", "repeated-linear-scan")),
                    stubRule("expensive-callback"),
                    stubRule("repeated-linear-scan"),
                )

            val result = applySubsumption(listOf(dominant, sub1, sub2), rules)

            result shouldHaveSize 1
            result.first().ruleId shouldBe "nested-lookup"
        }

        @Test
        fun `multiple dominant rules can each subsume different findings`() {
            val a = finding("nested-lookup", line = 10)
            val aSub = finding("expensive-callback", line = 10)
            val b = finding("redundant-expensive-call", line = 20)
            val bSub = finding("uncached-getter", line = 20)
            val rules =
                listOf(
                    stubRule("nested-lookup", subsumes = setOf("expensive-callback")),
                    stubRule("expensive-callback"),
                    stubRule("redundant-expensive-call", subsumes = setOf("uncached-getter")),
                    stubRule("uncached-getter"),
                )

            val result = applySubsumption(listOf(a, aSub, b, bSub), rules)

            result.map { it.ruleId }.shouldContainExactlyInAnyOrder("nested-lookup", "redundant-expensive-call")
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `empty findings list returns empty`() {
            val rules = listOf(stubRule("nested-lookup", subsumes = setOf("expensive-callback")))

            val result = applySubsumption(emptyList(), rules)

            result.shouldBeEmpty()
        }

        @Test
        fun `no subsumption declarations returns all findings unchanged`() {
            val findings = listOf(finding("a", line = 1), finding("b", line = 1))
            val rules = listOf(stubRule("a"), stubRule("b"))

            val result = applySubsumption(findings, rules)

            result shouldHaveSize 2
        }

        @Test
        fun `same rule at same location is not self-subsumed`() {
            val f1 = finding("nested-lookup", line = 42)
            val f2 = finding("nested-lookup", line = 42)
            val rules = listOf(stubRule("nested-lookup", subsumes = setOf("expensive-callback")))

            val result = applySubsumption(listOf(f1, f2), rules)

            result shouldHaveSize 2
        }
    }

    private fun finding(
        ruleId: String,
        line: Int,
    ) = Finding(
        ruleId = ruleId,
        ruleName = ruleId,
        severity = Severity.WARNING,
        location = SourceLocation(file, line, 1),
        message = "test",
        suggestions = listOf(Suggestion.Freeform("fix")),
    )

    private fun stubRule(
        id: String,
        subsumes: Set<String> = emptySet(),
    ) = object : Rule {
        override val id = id
        override val name = id
        override val severity = Severity.WARNING
        override val languages = Language.entries.toSet()
        override val category = RuleCategory.LOOP_AMPLIFIER
        override val subsumes = subsumes

        override fun evaluate(context: AnalysisContext) = emptyList<Finding>()
    }
}
