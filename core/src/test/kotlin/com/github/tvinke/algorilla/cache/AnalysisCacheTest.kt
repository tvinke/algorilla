package com.github.tvinke.algorilla.cache

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.config.RuleOverride
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AnalysisCacheTest {
    @TempDir
    lateinit var tempDir: File

    private fun entry(path: String = "Foo.java") = CachedFileEntry(filePath = path, contentHash = "abc123", findings = emptyList())

    @Test
    fun `cache hits when saved and reloaded with the same config`() {
        val config = AnalysisConfig(minConfidence = Confidence.HIGH)
        AnalysisCache(tempDir, config).save(listOf(entry()))

        val reloaded = AnalysisCache(tempDir, config).load()

        reloaded.shouldNotBeEmpty()
    }

    @Test
    fun `cache invalidates when minConfidence changes`() {
        AnalysisCache(tempDir, AnalysisConfig(minConfidence = Confidence.HIGH)).save(listOf(entry()))

        val reloaded = AnalysisCache(tempDir, AnalysisConfig(minConfidence = Confidence.LOW)).load()

        reloaded.shouldBeEmpty()
    }

    @Test
    fun `cache invalidates when a rule is disabled via config`() {
        AnalysisCache(tempDir, AnalysisConfig()).save(listOf(entry()))

        val withOverride =
            AnalysisConfig(ruleOverrides = mapOf("io-in-loop" to RuleOverride(enabled = false)))
        val reloaded = AnalysisCache(tempDir, withOverride).load()

        reloaded.shouldBeEmpty()
    }

    @Test
    fun `cache invalidates when minSeverity changes`() {
        AnalysisCache(tempDir, AnalysisConfig(minSeverity = Severity.WARNING)).save(listOf(entry()))

        val reloaded = AnalysisCache(tempDir, AnalysisConfig(minSeverity = Severity.ERROR)).load()

        reloaded.shouldBeEmpty()
    }

    @Test
    fun `cache still round-trips when no config is supplied at all`() {
        // Backward-compat path for callers that don't have an AnalysisConfig yet.
        AnalysisCache(tempDir).save(listOf(entry()))

        val reloaded = AnalysisCache(tempDir).load()

        reloaded.shouldNotBeEmpty()
    }
}
