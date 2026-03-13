package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.BuiltinRules
import java.io.File

/**
 * Base class for full-pipeline precision regression tests.
 *
 * Sets up an [AnalysisEngine] with [GroovyLanguageParser] and all builtin rules,
 * configured with minimum severity/confidence so every finding is visible.
 */
internal abstract class FullPipelineTestSupport {
    private val parser = GroovyLanguageParser()

    private val engine =
        AnalysisEngine(
            parsers = listOf(parser),
            rules = BuiltinRules.all(),
            config = AnalysisConfig(minSeverity = Severity.INFO, minConfidence = Confidence.LOW),
        )

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: fixtures/$fixturePath")
        val path = File(url.toURI()).absolutePath
        return engine.analyze(listOf(path)).findings
    }

    protected fun assertNoFindings(
        fixturePath: String,
        ruleId: String,
        reason: String,
    ) {
        val findings = analyzeFixture(fixturePath).filter { it.ruleId == ruleId }
        assert(findings.isEmpty()) {
            "Expected no findings for rule '$ruleId' ($reason), but got ${findings.size}:\n" +
                findings.joinToString("\n") { "  - ${it.message} (${it.severity}, ${it.confidence})" }
        }
    }

    protected fun assertHasFindings(
        fixturePath: String,
        ruleId: String,
        reason: String,
    ) {
        val findings = analyzeFixture(fixturePath).filter { it.ruleId == ruleId }
        assert(findings.isNotEmpty()) {
            "Expected at least one finding for rule '$ruleId' ($reason), but got none"
        }
    }
}
