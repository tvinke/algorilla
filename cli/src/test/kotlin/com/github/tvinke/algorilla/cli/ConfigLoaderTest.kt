package com.github.tvinke.algorilla.cli

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

internal class ConfigLoaderTest {
    @Nested
    inner class LenientParsing {
        @Test
        fun `ignores unknown top-level keys`(
            @TempDir tmp: File,
        ) {
            val config = File(tmp, ".algorilla.yml")
            config.writeText(
                """
                min-severity: info
                future-key: some-value
                another-unknown:
                  nested: true
                """.trimIndent(),
            )
            val result = loadConfig(config)
            result.minSeverity.name shouldBe "INFO"
        }

        @Test
        fun `loads valid config without errors`(
            @TempDir tmp: File,
        ) {
            val config = File(tmp, ".algorilla.yml")
            config.writeText(
                """
                min-severity: warning
                exclude:
                  - "**/generated/**"
                rules:
                  nested-lookup:
                    enabled: false
                """.trimIndent(),
            )
            val result = loadConfig(config)
            result.excludePatterns shouldBe listOf("**/generated/**")
            result.ruleOverrides["nested-lookup"]?.enabled shouldBe false
        }

        @Test
        fun `returns default config when file does not exist`() {
            val result = loadConfig(File("nonexistent.yml"))
            result.excludePatterns shouldBe emptyList()
        }

        @Test
        fun `returns default config when no file given and no default present`() {
            val result = loadConfig(null)
            result.excludePatterns shouldBe emptyList()
        }
    }

    @Nested
    inner class MinConfidence {
        @Test
        fun `parses min-confidence from config`(
            @TempDir tmp: File,
        ) {
            val config = File(tmp, ".algorilla.yml")
            config.writeText(
                """
                min-confidence: high
                """.trimIndent(),
            )
            val result = loadConfig(config)
            result.minConfidence.name shouldBe "HIGH"
        }

        @Test
        fun `defaults to MEDIUM when not specified`() {
            val result = loadConfig(null)
            result.minConfidence.name shouldBe "MEDIUM"
        }
    }

    @Nested
    inner class ExperimentalKeyWarnings {
        @Test
        fun `warns when type-hints is used`(
            @TempDir tmp: File,
        ) {
            val config = File(tmp, ".algorilla.yml")
            config.writeText(
                """
                type-hints:
                  myCache: HashMap
                """.trimIndent(),
            )
            val stderr = captureStderr { loadConfig(config) }
            stderr shouldContain "'type-hints' is experimental"
        }

        @Test
        fun `warns when heavyweight-types is used`(
            @TempDir tmp: File,
        ) {
            val config = File(tmp, ".algorilla.yml")
            config.writeText(
                """
                heavyweight-types:
                  - CustomMapper
                """.trimIndent(),
            )
            val stderr = captureStderr { loadConfig(config) }
            stderr shouldContain "'heavyweight-types' is experimental"
        }

        @Test
        fun `no warning for standard keys only`(
            @TempDir tmp: File,
        ) {
            val config = File(tmp, ".algorilla.yml")
            config.writeText(
                """
                min-severity: warning
                """.trimIndent(),
            )
            val stderr = captureStderr { loadConfig(config) }
            stderr shouldBe ""
        }

        private fun captureStderr(block: () -> Unit): String {
            val original = System.err
            val baos = ByteArrayOutputStream()
            System.setErr(PrintStream(baos))
            try {
                block()
            } finally {
                System.setErr(original)
            }
            return baos.toString().trim()
        }
    }
}
