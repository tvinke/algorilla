package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ConfidenceAdjustmentTest {
    private fun finding(
        ruleId: String = "nested-lookup",
        file: String = "/src/Main.java",
        evidence: List<Evidence> = emptyList(),
    ) = Finding(
        ruleId = ruleId,
        ruleName = "Test Rule",
        severity = Severity.WARNING,
        location = SourceLocation(file, 10, 1),
        message = "Test finding",
        suggestion = "Fix it",
        evidence = evidence,
    )

    @Nested
    inner class StructuralHighPromotion {
        @ParameterizedTest
        @ValueSource(
            strings = [
                "string-concat-in-loop",
                "sort-for-last",
                "in-loop-collection-building",
                "filter-after-sort",
                "expensive-sort-comparator",
                "repeated-regex-in-loop",
                "repeated-reflection-in-loop",
                "quadratic-removal",
                "sequential-async-join-in-loop",
            ],
        )
        fun `structural rules are promoted to HIGH`(ruleId: String) {
            val findings = listOf(finding(ruleId = ruleId))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.HIGH
        }

        @Test
        fun `structural rules are HIGH even in JS files`() {
            val findings = listOf(finding(ruleId = "string-concat-in-loop", file = "/src/app.js"))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.HIGH
        }
    }

    @Nested
    inner class JsTsDemotion {
        @ParameterizedTest
        @ValueSource(strings = ["js", "jsx", "ts", "tsx", "vue", "mjs", "cjs"])
        fun `non-structural rules in JS-TS files are demoted to LOW`(ext: String) {
            val findings = listOf(finding(ruleId = "nested-lookup", file = "/src/app.$ext"))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.LOW
        }

        @Test
        fun `Java files stay at MEDIUM`() {
            val findings = listOf(finding(ruleId = "nested-lookup", file = "/src/Main.java"))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }

        @Test
        fun `Kotlin files stay at MEDIUM`() {
            val findings = listOf(finding(ruleId = "nested-lookup", file = "/src/Main.kt"))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }
    }

    @Nested
    inner class CrossMethodFindings {
        @Test
        fun `cross-file evidence keeps finding at MEDIUM`() {
            val evidence =
                listOf(
                    Evidence(
                        location = SourceLocation("/src/Other.java", 20, 1),
                        label = "calls helper()",
                        executionContext = ExecutionContext.SINGLE,
                        depth = 1,
                    ),
                )
            val findings = listOf(finding(ruleId = "nested-lookup", evidence = evidence))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }

        @Test
        fun `same-file evidence does not prevent promotion for structural rules`() {
            val evidence =
                listOf(
                    Evidence(
                        location = SourceLocation("/src/Main.java", 20, 1),
                        label = "loop body",
                        executionContext = ExecutionContext.INSIDE_LOOP,
                    ),
                )
            val findings = listOf(finding(ruleId = "string-concat-in-loop", evidence = evidence))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.HIGH
        }
    }

    @Nested
    inner class EmptyAndPreserve {
        @Test
        fun `empty list returns empty`() {
            adjustConfidence(emptyList()) shouldBe emptyList()
        }

        @Test
        fun `non-structural JVM rule stays MEDIUM`() {
            val findings = listOf(finding(ruleId = "hidden-nested-loop"))
            val adjusted = adjustConfidence(findings)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }
    }
}
