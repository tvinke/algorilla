package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ConfidenceAdjustmentTest {
    /** Creates a stub [Rule] with the given metadata for use in the ruleIndex. */
    private fun stubRule(
        id: String,
        defaultConfidence: Confidence = Confidence.MEDIUM,
        requiresTypeContext: Boolean = false,
    ): Rule =
        object : Rule {
            override val id: String = id
            override val name: String = id
            override val severity: Severity = Severity.WARNING
            override val languages: Set<Language> = Language.entries.toSet()
            override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER
            override val defaultConfidence: Confidence = defaultConfidence
            override val requiresTypeContext: Boolean = requiresTypeContext

            override fun evaluate(context: AnalysisContext): List<Finding> = emptyList()
        }

    /** Builds a ruleIndex from a list of stub rules. */
    private fun ruleIndexOf(vararg rules: Rule): Map<String, Rule> = rules.associateBy { it.id }

    /** Default ruleIndex containing the standard rule metadata used across tests. */
    private val defaultRuleIndex: Map<String, Rule> =
        ruleIndexOf(
            stubRule("string-concat-in-loop", Confidence.HIGH, requiresTypeContext = true),
            stubRule("sort-for-last", Confidence.HIGH),
            stubRule("in-loop-collection-building", Confidence.HIGH),
            stubRule("filter-after-sort", Confidence.HIGH),
            stubRule("expensive-sort-comparator", Confidence.HIGH),
            stubRule("repeated-regex-in-loop", Confidence.HIGH),
            stubRule("quadratic-removal", Confidence.HIGH, requiresTypeContext = true),
            stubRule("sequential-async-join-in-loop", Confidence.HIGH),
            stubRule("expensive-callback", Confidence.LOW, requiresTypeContext = true),
            stubRule("chained-getters", Confidence.LOW, requiresTypeContext = true),
            stubRule("nested-lookup"),
            stubRule("hidden-nested-loop"),
        )

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
        suggestions = listOf(Suggestion.Freeform("Fix it")),
        evidence = evidence,
    )

    private fun languagesFor(vararg files: String): Map<String, Language> =
        files
            .mapNotNull { file ->
                val ext = file.substringAfterLast('.')
                Language.fromExtension(ext)?.let { file to it }
            }.toMap()

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
                "quadratic-removal",
                "sequential-async-join-in-loop",
            ],
        )
        fun `structural rules are promoted to HIGH`(ruleId: String) {
            val file = "/src/Main.java"
            val findings = listOf(finding(ruleId = ruleId, file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.HIGH
        }

        @Test
        fun `structural rules without type-context requirement are HIGH in JS files`() {
            val file = "/src/app.js"
            val findings = listOf(finding(ruleId = "sort-for-last", file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.HIGH
        }
    }

    @Nested
    inner class UntypedLanguageDemotion {
        @ParameterizedTest
        @ValueSource(strings = ["js", "vue", "mjs", "cjs"])
        fun `non-structural rules in untyped languages are demoted to LOW`(ext: String) {
            val file = "/src/app.$ext"
            val findings = listOf(finding(ruleId = "nested-lookup", file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.LOW
        }

        @Test
        fun `Java files stay at MEDIUM`() {
            val file = "/src/Main.java"
            val findings = listOf(finding(ruleId = "nested-lookup", file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }

        @Test
        fun `Kotlin files stay at MEDIUM`() {
            val file = "/src/Main.kt"
            val findings = listOf(finding(ruleId = "nested-lookup", file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }

        @Test
        fun `TypeScript files stay at MEDIUM`() {
            val file = "/src/app.ts"
            val findings = listOf(finding(ruleId = "nested-lookup", file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }
    }

    @Nested
    inner class CrossMethodFindings {
        @Test
        fun `cross-file evidence keeps finding at MEDIUM`() {
            val file = "/src/Main.java"
            val evidence =
                listOf(
                    Evidence(
                        location = SourceLocation("/src/Other.java", 20, 1),
                        label = "calls helper()",
                        executionContext = ExecutionContext.SINGLE,
                        depth = 1,
                    ),
                )
            val findings = listOf(finding(ruleId = "nested-lookup", file = file, evidence = evidence))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }

        @Test
        fun `same-file evidence does not prevent promotion for structural rules`() {
            val file = "/src/Main.java"
            val evidence =
                listOf(
                    Evidence(
                        location = SourceLocation(file, 20, 1),
                        label = "loop body",
                        executionContext = ExecutionContext.INSIDE_LOOP,
                    ),
                )
            val findings = listOf(finding(ruleId = "string-concat-in-loop", file = file, evidence = evidence))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.HIGH
        }
    }

    @Nested
    inner class HeuristicHeavyDemotion {
        @ParameterizedTest
        @ValueSource(strings = ["expensive-callback", "chained-getters"])
        fun `heuristic-heavy rules are demoted to LOW`(ruleId: String) {
            val file = "/src/Main.java"
            val findings = listOf(finding(ruleId = ruleId, file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.LOW
        }
    }

    @Nested
    inner class EmptyAndPreserve {
        @Test
        fun `empty list returns empty`() {
            adjustConfidence(emptyList(), emptyMap(), emptyMap()) shouldBe emptyList()
        }

        @Test
        fun `non-structural JVM rule stays MEDIUM`() {
            val file = "/src/Main.java"
            val findings = listOf(finding(ruleId = "hidden-nested-loop", file = file))
            val adjusted = adjustConfidence(findings, languagesFor(file), defaultRuleIndex)
            adjusted.first().confidence shouldBe Confidence.MEDIUM
        }
    }
}
