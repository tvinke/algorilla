package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class SuppressionFilterTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `should suppress finding by alias`() {
        val source = tempDir.resolve("Test.java")
        source.writeText("List items = getAll(); // algorilla:ignore old-rule-name\n")

        val finding = finding(source.absolutePath, line = 1, ruleId = "new-rule-name")
        val aliasIndex = mapOf("old-rule-name" to "new-rule-name")

        val result = SuppressionFilter().filter(listOf(finding), emptyMap(), aliasIndex)

        result.shouldBeEmpty()
    }

    @Test
    fun `should not suppress when alias maps to different rule`() {
        val source = tempDir.resolve("Test.java")
        source.writeText("List items = getAll(); // algorilla:ignore old-rule-name\n")

        val finding = finding(source.absolutePath, line = 1, ruleId = "unrelated-rule")
        val aliasIndex = mapOf("old-rule-name" to "new-rule-name")

        val result = SuppressionFilter().filter(listOf(finding), emptyMap(), aliasIndex)

        result shouldHaveSize 1
    }

    @Test
    fun `should still suppress by canonical rule id`() {
        val source = tempDir.resolve("Test.java")
        source.writeText("List items = getAll(); // algorilla:ignore new-rule-name\n")

        val finding = finding(source.absolutePath, line = 1, ruleId = "new-rule-name")

        val result = SuppressionFilter().filter(listOf(finding), emptyMap())

        result.shouldBeEmpty()
    }

    @Nested
    inner class CrossFileEvidence {
        @Test
        fun `suppresses when evidence in another file has ignore comment`() {
            val mainFile = tempDir.resolve("Main.java")
            mainFile.writeText("findAll(items);\n")

            val evidenceFile = tempDir.resolve("Helper.java")
            evidenceFile.writeText("// algorilla:ignore\nfor (Item i : items) { list.contains(i); }\n")

            val f =
                finding(
                    mainFile.absolutePath,
                    line = 1,
                    ruleId = "nested-lookup",
                    evidence =
                        listOf(
                            Evidence(
                                location = SourceLocation(evidenceFile.absolutePath, 2, 1),
                                label = "contains call",
                                executionContext = ExecutionContext.INSIDE_LOOP,
                            ),
                        ),
                )

            val result = SuppressionFilter().filter(listOf(f), emptyMap())
            result.shouldBeEmpty()
        }

        @Test
        fun `does not suppress cross-file when no ignore comment present`() {
            val mainFile = tempDir.resolve("Main.java")
            mainFile.writeText("findAll(items);\n")

            val evidenceFile = tempDir.resolve("Helper.java")
            evidenceFile.writeText("for (Item i : items) { list.contains(i); }\n")

            val f =
                finding(
                    mainFile.absolutePath,
                    line = 1,
                    ruleId = "nested-lookup",
                    evidence =
                        listOf(
                            Evidence(
                                location = SourceLocation(evidenceFile.absolutePath, 1, 1),
                                label = "contains call",
                                executionContext = ExecutionContext.INSIDE_LOOP,
                            ),
                        ),
                )

            val result = SuppressionFilter().filter(listOf(f), emptyMap())
            result shouldHaveSize 1
        }
    }

    @Nested
    inner class FileLevelSuppression {
        @Test
        fun `ignore-file suppresses all rules in that file`() {
            val source = tempDir.resolve("Legacy.java")
            source.writeText("// algorilla:ignore-file\npackage com.example;\nList.contains(x);\n")

            val f = finding(source.absolutePath, line = 3, ruleId = "nested-lookup")
            val result = SuppressionFilter().filter(listOf(f), emptyMap())

            result.shouldBeEmpty()
        }

        @Test
        fun `ignore-file with rule id suppresses only that rule`() {
            val source = tempDir.resolve("Legacy.java")
            source.writeText("// algorilla:ignore-file nested-lookup\npackage com.example;\nList.contains(x);\n")

            val f1 = finding(source.absolutePath, line = 3, ruleId = "nested-lookup")
            val f2 = finding(source.absolutePath, line = 3, ruleId = "sort-for-last")
            val result = SuppressionFilter().filter(listOf(f1, f2), emptyMap())

            result shouldHaveSize 1
            result[0].ruleId == "sort-for-last"
        }

        @Test
        fun `ignore-file only checked in first 5 lines`() {
            val source = tempDir.resolve("Deep.java")
            val lines = (1..6).map { "// line $it" }.toMutableList()
            lines.add("// algorilla:ignore-file") // line 7 — too deep
            lines.add("List.contains(x);")
            source.writeText(lines.joinToString("\n"))

            val f = finding(source.absolutePath, line = 8, ruleId = "nested-lookup")
            val result = SuppressionFilter().filter(listOf(f), emptyMap())

            result shouldHaveSize 1
        }

        @Test
        fun `ignore-file works with block comment`() {
            val source = tempDir.resolve("Legacy.java")
            source.writeText("/* algorilla:ignore-file */\npackage com.example;\nList.contains(x);\n")

            val f = finding(source.absolutePath, line = 3, ruleId = "nested-lookup")
            val result = SuppressionFilter().filter(listOf(f), emptyMap())

            result.shouldBeEmpty()
        }
    }

    private fun finding(
        file: String,
        line: Int,
        ruleId: String,
        evidence: List<Evidence> = emptyList(),
    ) = Finding(
        ruleId = ruleId,
        ruleName = "Test Rule",
        severity = com.github.tvinke.algorilla.model.Severity.WARNING,
        location = SourceLocation(file, line, 1),
        message = "test finding",
        suggestions = listOf(Suggestion.Freeform("fix it")),
        evidence = evidence,
    )
}
