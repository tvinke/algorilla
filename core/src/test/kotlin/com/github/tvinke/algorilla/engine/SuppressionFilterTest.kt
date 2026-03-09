package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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

    private fun finding(file: String, line: Int, ruleId: String) = Finding(
        ruleId = ruleId,
        ruleName = "Test Rule",
        severity = com.github.tvinke.algorilla.model.Severity.WARNING,
        location = SourceLocation(file, line, 1),
        message = "test finding",
        suggestion = "fix it",
    )
}
