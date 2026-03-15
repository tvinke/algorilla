package com.github.tvinke.algorilla.lang.javascript.parser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Full-pipeline precision regression tests for JavaScript/TypeScript fixtures.
 *
 * Every negative fixture is run through the complete AnalysisEngine pipeline
 * to catch regressions from TypeEnvironment, subsumption, confidence adjustment,
 * or cross-rule interactions.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class RedundantExpensiveCallRegressions {
        @Test
        fun `geometry utilities are too cheap to flag`() =
            assertNoFindings(
                "redundant-expensive-call/regression/geometry-utils.js",
                "redundant-expensive-call",
                "Geometry utility functions (pointFrom, vectorCross) are O(1)",
            )
    }
}
