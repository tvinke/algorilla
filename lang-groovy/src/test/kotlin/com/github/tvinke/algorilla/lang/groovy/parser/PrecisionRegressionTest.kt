package com.github.tvinke.algorilla.lang.groovy.parser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Full-pipeline precision regression tests for Groovy fixtures.
 *
 * Every negative fixture is run through the complete AnalysisEngine pipeline
 * to catch regressions from TypeEnvironment, subsumption, confidence adjustment,
 * or cross-rule interactions.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class ImplicitRegexRegressions {
        @Test
        fun `Predicate matches is not regex`() =
            assertNoFindings(
                "implicit-regex-in-loop/regression/predicate-matches.groovy",
                "implicit-regex-in-loop",
                "Predicate.matches() is not a String regex operation",
            )
    }
}
