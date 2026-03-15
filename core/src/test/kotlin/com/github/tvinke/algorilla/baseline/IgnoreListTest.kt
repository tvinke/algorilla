package com.github.tvinke.algorilla.baseline

import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class IgnoreListTest {
    private fun finding(
        ruleId: String = "nested-lookup",
        file: String = "/src/Main.java",
        line: Int = 10,
        message: String = "Linear lookup inside loop",
    ) = Finding(
        ruleId = ruleId,
        ruleName = "Nested Lookup",
        severity = Severity.WARNING,
        location = SourceLocation(file, line, 1),
        message = message,
        suggestions = listOf(Suggestion.Freeform("Use a HashSet")),
    )

    @Nested
    inner class Filter {
        @Test
        fun `empty ignore list returns all findings`() {
            val ignoreList = IgnoreList.load(File("nonexistent.json"))
            val findings = listOf(finding(), finding(ruleId = "sort-for-last"))

            ignoreList.filter(findings) shouldHaveSize 2
        }

        @Test
        fun `filters out accepted findings`(
            @TempDir tmp: File,
        ) {
            val f1 = finding()
            val f2 = finding(ruleId = "sort-for-last", message = "Sort for last element")
            val ignoreFile = File(tmp, "ignore-list.json")

            val hash1 = Baseline.fingerprintOf(f1).contentHash
            IgnoreList.accept(ignoreFile, listOf(f1, f2), setOf(hash1))

            val loaded = IgnoreList.load(ignoreFile)
            val filtered = loaded.filter(listOf(f1, f2))

            filtered shouldHaveSize 1
            filtered[0].ruleId shouldBe "sort-for-last"
        }
    }

    @Nested
    inner class Accept {
        @Test
        fun `accept creates ignore list file`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, ".algorilla/ignore-list.json")
            val f = finding()
            val hash = Baseline.fingerprintOf(f).contentHash

            val count = IgnoreList.accept(ignoreFile, listOf(f), setOf(hash))

            count shouldBe 1
            ignoreFile.exists() shouldBe true
        }

        @Test
        fun `accept merges with existing entries`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            val f1 = finding()
            val f2 = finding(ruleId = "sort-for-last", message = "Sort for last element")
            val hash1 = Baseline.fingerprintOf(f1).contentHash
            val hash2 = Baseline.fingerprintOf(f2).contentHash

            IgnoreList.accept(ignoreFile, listOf(f1), setOf(hash1))
            IgnoreList.accept(ignoreFile, listOf(f2), setOf(hash2))

            val loaded = IgnoreList.load(ignoreFile)
            loaded.size shouldBe 2
        }

        @Test
        fun `accept with unknown hash returns zero`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            val count = IgnoreList.accept(ignoreFile, listOf(finding()), setOf("deadbeef"))

            count shouldBe 0
        }

        @Test
        fun `accept is idempotent for same finding`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            val f = finding()
            val hash = Baseline.fingerprintOf(f).contentHash

            IgnoreList.accept(ignoreFile, listOf(f), setOf(hash))
            IgnoreList.accept(ignoreFile, listOf(f), setOf(hash))

            val loaded = IgnoreList.load(ignoreFile)
            loaded.size shouldBe 1
        }
    }

    @Nested
    inner class LoadSave {
        @Test
        fun `load returns empty for missing file`() {
            val ignoreList = IgnoreList.load(File("does-not-exist.json"))
            ignoreList.size shouldBe 0
        }

        @Test
        fun `load returns empty for corrupted file`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            ignoreFile.writeText("not json at all {{{")

            val ignoreList = IgnoreList.load(ignoreFile)
            ignoreList.size shouldBe 0
        }

        @Test
        fun `default file resolves under project root`(
            @TempDir tmp: File,
        ) {
            val file = IgnoreList.defaultFile(tmp)
            file.path shouldBe File(tmp, ".algorilla/ignore-list.json").path
        }
    }

    @Nested
    inner class Compat {
        @Test
        fun `ignores unknown fields in ignore list JSON`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            ignoreFile.writeText(
                """
                {
                    "version": 1,
                    "futureField": "some-value",
                    "ignored": [
                        {
                            "fingerprint": "abc123",
                            "ruleId": "nested-lookup",
                            "file": "/src/Main.java",
                            "message": "Linear lookup inside loop",
                            "unknownNested": true
                        }
                    ]
                }
                """.trimIndent(),
            )
            val ignoreList = IgnoreList.load(ignoreFile)
            ignoreList.size shouldBe 1
        }

        @Test
        fun `loads ignore list without version field`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            ignoreFile.writeText(
                """
                {
                    "ignored": [
                        {
                            "fingerprint": "abc123",
                            "ruleId": "nested-lookup",
                            "file": "/src/Main.java",
                            "message": "Linear lookup inside loop"
                        }
                    ]
                }
                """.trimIndent(),
            )
            val ignoreList = IgnoreList.load(ignoreFile)
            ignoreList.size shouldBe 1
        }

        @Test
        fun `saved ignore list includes version field`(
            @TempDir tmp: File,
        ) {
            val ignoreFile = File(tmp, "ignore-list.json")
            val f = finding()
            val hash = Baseline.fingerprintOf(f).contentHash
            IgnoreList.accept(ignoreFile, listOf(f), setOf(hash))

            val content = ignoreFile.readText()
            content shouldContain "\"version\""
        }
    }
}
