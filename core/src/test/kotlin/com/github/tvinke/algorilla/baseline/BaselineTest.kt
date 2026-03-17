package com.github.tvinke.algorilla.baseline

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class BaselineTest {
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
    inner class Compat {
        @Test
        fun `loads baseline without version field`(
            @TempDir tmp: File,
        ) {
            val baselineFile = File(tmp, "baseline.json")
            baselineFile.writeText(
                """
                {
                    "fingerprints": [
                        {
                            "file": "/src/Main.java",
                            "ruleId": "nested-lookup",
                            "contentHash": "abc123"
                        }
                    ]
                }
                """.trimIndent(),
            )
            val baseline = Baseline.load(baselineFile)
            baseline.filterNew(emptyList()) shouldHaveSize 0
        }

        @Test
        fun `loads baseline with version field`(
            @TempDir tmp: File,
        ) {
            val baselineFile = File(tmp, "baseline.json")
            baselineFile.writeText(
                """
                {
                    "version": 1,
                    "fingerprints": [
                        {
                            "file": "/src/Main.java",
                            "ruleId": "nested-lookup",
                            "contentHash": "abc123"
                        }
                    ]
                }
                """.trimIndent(),
            )
            val baseline = Baseline.load(baselineFile)
            baseline.filterNew(emptyList()) shouldHaveSize 0
        }

        @Test
        fun `ignores unknown fields in baseline JSON`(
            @TempDir tmp: File,
        ) {
            val baselineFile = File(tmp, "baseline.json")
            baselineFile.writeText(
                """
                {
                    "version": 1,
                    "futureField": "some-value",
                    "fingerprints": [
                        {
                            "file": "/src/Main.java",
                            "ruleId": "nested-lookup",
                            "contentHash": "abc123",
                            "unknownNested": 42
                        }
                    ]
                }
                """.trimIndent(),
            )
            val baseline = Baseline.load(baselineFile)
            baseline.filterNew(emptyList()) shouldHaveSize 0
        }

        @Test
        fun `saved baseline includes version field`(
            @TempDir tmp: File,
        ) {
            val baselineFile = File(tmp, "baseline.json")
            val f = finding()
            Baseline.save(listOf(f), baselineFile)

            val content = baselineFile.readText()
            content shouldContain "\"version\""
        }
    }

    @Nested
    inner class ConfidenceIndependence {
        @Test
        fun `fingerprint is identical regardless of confidence`() {
            val low =
                Finding(
                    ruleId = "nested-lookup",
                    ruleName = "Nested Lookup",
                    severity = Severity.WARNING,
                    confidence = Confidence.LOW,
                    location = SourceLocation("/src/Main.java", 10, 1),
                    message = "Linear lookup inside loop",
                    suggestions = listOf(Suggestion.Freeform("Use a HashSet")),
                )
            val high = low.copy(confidence = Confidence.HIGH)

            Baseline.fingerprintOf(low) shouldBe Baseline.fingerprintOf(high)
        }
    }

    @Nested
    inner class FilterNew {
        @Test
        fun `empty baseline returns all findings`() {
            val baseline = Baseline.load(File("nonexistent.json"))
            val findings = listOf(finding(), finding(ruleId = "sort-for-last"))
            baseline.filterNew(findings) shouldHaveSize 2
        }

        @Test
        fun `filters out known findings`(
            @TempDir tmp: File,
        ) {
            val baselineFile = File(tmp, "baseline.json")
            val f1 = finding()
            val f2 = finding(ruleId = "sort-for-last", message = "Sort for last element")
            Baseline.save(listOf(f1), baselineFile)

            val baseline = Baseline.load(baselineFile)
            val filtered = baseline.filterNew(listOf(f1, f2))

            filtered shouldHaveSize 1
            filtered[0].ruleId shouldBe "sort-for-last"
        }

        @Test
        fun `load returns empty for missing file`() {
            val baseline = Baseline.load(File("does-not-exist.json"))
            baseline.filterNew(listOf(finding())) shouldHaveSize 1
        }

        @Test
        fun `load returns empty for corrupted file`(
            @TempDir tmp: File,
        ) {
            val baselineFile = File(tmp, "baseline.json")
            baselineFile.writeText("not json {{{")
            val baseline = Baseline.load(baselineFile)
            baseline.filterNew(listOf(finding())) shouldHaveSize 1
        }
    }

    @Nested
    inner class PortableFingerprints {
        @Test
        fun `fingerprint uses relative path when projectRoot given`(
            @TempDir tmp: File,
        ) {
            val projectRoot = File(tmp, "project")
            projectRoot.mkdirs()
            val f = finding(file = "${projectRoot.absolutePath}/src/Main.java")
            val fp = Baseline.fingerprintOf(f, projectRoot)
            fp.file shouldBe "src/Main.java"
            fp.file shouldNotContain projectRoot.absolutePath
        }

        @Test
        fun `fingerprint uses absolute path when projectRoot is null`() {
            val f = finding(file = "/home/user/project/src/Main.java")
            val fp = Baseline.fingerprintOf(f)
            fp.file shouldBe "/home/user/project/src/Main.java"
        }

        @Test
        fun `fingerprint normalizes quoted literals in message`() {
            val f1 = finding(message = "Linear lookup on 'users' inside loop")
            val f2 = finding(message = "Linear lookup on 'items' inside loop")
            Baseline.fingerprintOf(f1).contentHash shouldBe Baseline.fingerprintOf(f2).contentHash
        }

        @Test
        fun `fingerprint normalizes double-quoted literals`() {
            val f1 = finding(message = """Repeated call to "getItems" in loop""")
            val f2 = finding(message = """Repeated call to "fetchAll" in loop""")
            Baseline.fingerprintOf(f1).contentHash shouldBe Baseline.fingerprintOf(f2).contentHash
        }

        @Test
        fun `different rule IDs produce different fingerprints`() {
            val f1 = finding(ruleId = "nested-lookup")
            val f2 = finding(ruleId = "sort-for-last")
            Baseline.fingerprintOf(f1).contentHash shouldNotBe Baseline.fingerprintOf(f2).contentHash
        }

        @Test
        fun `same finding produces same fingerprint across machines`(
            @TempDir tmp: File,
        ) {
            val root1 = File(tmp, "machine-a/project")
            root1.mkdirs()
            val root2 = File(tmp, "machine-b/project")
            root2.mkdirs()
            val f1 = finding(file = "${root1.absolutePath}/src/Main.java")
            val f2 = finding(file = "${root2.absolutePath}/src/Main.java")
            val fp1 = Baseline.fingerprintOf(f1, root1)
            val fp2 = Baseline.fingerprintOf(f2, root2)
            fp1.contentHash shouldBe fp2.contentHash
        }
    }

    @Nested
    inner class V1Migration {
        @Test
        fun `filterNew matches v1 fingerprints from old baseline`(
            @TempDir tmp: File,
        ) {
            val projectRoot = File(tmp, "project")
            projectRoot.mkdirs()
            val absolutePath = "${projectRoot.absolutePath}/src/Main.java"
            val f = finding(file = absolutePath)

            // Save baseline with v1 fingerprint (absolute path, raw message)
            val v1Fp = Baseline.fingerprintV1(f)
            val baselineFile = File(tmp, "baseline.json")
            baselineFile.writeText(
                """
                {
                    "version": 1,
                    "fingerprints": [
                        {
                            "file": "$absolutePath",
                            "ruleId": "${v1Fp.ruleId}",
                            "contentHash": "${v1Fp.contentHash}"
                        }
                    ]
                }
                """.trimIndent(),
            )

            val baseline = Baseline.load(baselineFile)
            // filterNew with projectRoot should still recognize v1 entries
            baseline.filterNew(listOf(f), projectRoot) shouldHaveSize 0
        }

        @Test
        fun `saved baseline uses v2 fingerprints with relative paths`(
            @TempDir tmp: File,
        ) {
            val projectRoot = File(tmp, "project")
            projectRoot.mkdirs()
            val absolutePath = "${projectRoot.absolutePath}/src/Main.java"
            val f = finding(file = absolutePath)

            val baselineFile = File(tmp, "baseline.json")
            Baseline.save(listOf(f), baselineFile, projectRoot)

            val content = baselineFile.readText()
            content shouldContain "src/Main.java"
            content shouldNotContain projectRoot.absolutePath
        }
    }
}
