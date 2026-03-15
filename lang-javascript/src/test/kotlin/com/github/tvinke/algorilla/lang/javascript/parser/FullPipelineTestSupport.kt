package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.BuiltinRules
import java.io.File

/**
 * Shared test support for running JavaScript/TypeScript fixtures through the full analysis pipeline.
 */
internal open class FullPipelineTestSupport {
    private val parser = JavaScriptLanguageParser()

    private fun engine(
        minSeverity: Severity = Severity.INFO,
        minConfidence: Confidence = Confidence.LOW,
    ) = AnalysisEngine(
        parsers = listOf(parser),
        rules = BuiltinRules.all(),
        config = AnalysisConfig(minSeverity = minSeverity, minConfidence = minConfidence),
    )

    protected fun analyzeFixture(
        fixturePath: String,
        minSeverity: Severity = Severity.INFO,
        minConfidence: Confidence = Confidence.LOW,
    ): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        return engine(minSeverity, minConfidence).analyze(listOf(path)).findings
    }

    protected fun assertNoFindings(
        fixturePath: String,
        ruleId: String,
        reason: String,
    ) {
        val findings = analyzeFixture(fixturePath)
        val violations = findings.filter { it.ruleId == ruleId }
        if (violations.isNotEmpty()) {
            val lines = violations.joinToString { "line ${it.location.line}" }
            error(
                "Precision regression: $fixturePath produced unexpected $ruleId " +
                    "finding on $lines — $reason",
            )
        }
    }

    protected fun assertHasFindings(
        fixturePath: String,
        ruleId: String,
        reason: String,
    ) {
        val findings = analyzeFixture(fixturePath)
        val matches = findings.filter { it.ruleId == ruleId }
        if (matches.isEmpty()) {
            error(
                "Recall regression: $fixturePath produced no $ruleId findings " +
                    "through full pipeline — $reason",
            )
        }
    }
}
