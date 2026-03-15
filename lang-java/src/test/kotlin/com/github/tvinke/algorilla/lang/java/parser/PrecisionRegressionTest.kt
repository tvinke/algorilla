package com.github.tvinke.algorilla.lang.java.parser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Full-pipeline precision regression tests for Java fixtures.
 *
 * Every negative fixture is run through the complete AnalysisEngine pipeline
 * (not just the individual rule) to catch regressions from TypeEnvironment,
 * subsumption, confidence adjustment, or cross-rule interactions.
 *
 * Every positive fixture is a TP anchor to catch false-negative regressions.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class ImplicitRegexRegressions {
        @Test
        fun `Predicate matches is not regex`() =
            assertNoFindings(
                "implicit-regex-in-loop/regression/predicate-matches.java",
                "implicit-regex-in-loop",
                "Predicate.matches() is not a String regex operation",
            )
    }

    @Nested
    inner class TypedLoopVarRegressions {
        @Test
        fun `typed loop var String contains is not collection lookup`() =
            assertNoFindings(
                "quadratic-removal/regression/typed-loop-var-map-remove.java",
                "nested-lookup",
                "String loop variable .contains() is a string op after TypeEnvironment inference",
            )
    }

    @Nested
    inner class ExpensiveCallbackRegressions {
        @Test
        fun `recursive tree walk with forEach is not quadratic`() =
            assertNoFindings(
                "expensive-callback/regression/recursive-tree-walk-callback.java",
                "expensive-callback",
                "Recursive tree-walk forEach is O(tree_size), not O(n^2)",
            )
    }

    @Nested
    inner class RedundantExpensiveCallRegressions {
        @Test
        fun `type check predicates are too cheap to flag`() =
            assertNoFindings(
                "redundant-expensive-call/regression/type-check-predicates.java",
                "redundant-expensive-call",
                "is*/has* type-check predicates are O(1) — not worth flagging as redundant",
            )
    }

    // ── Positive fixtures: TP anchors ──────────────────────────────────────

    @Nested
    inner class ImplicitRegexAnchors {
        @Test
        fun `matches in loop`() =
            assertHasFindings(
                "implicit-regex-in-loop/positive/matches-in-loop.java",
                "implicit-regex-in-loop",
                "String.matches() in loop compiles regex each iteration",
            )
    }
}
