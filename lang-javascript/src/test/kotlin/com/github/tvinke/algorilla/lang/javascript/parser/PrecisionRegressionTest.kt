package com.github.tvinke.algorilla.lang.javascript.parser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Full-pipeline precision regression tests for JavaScript/TypeScript fixtures.
 *
 * Catches regressions from TypeEnvironment, YAML semantics, and cross-rule interactions.
 * JavaScript lacks type declarations; TypeScript fixtures test typed-variable inference.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class CardinalityExplosionNegatives {
        @Test
        fun `single loop`() =
            assertNoFindings(
                "cardinality-explosion/negative/single-loop.js",
                "cardinality-explosion",
                "single loop iteration is not a Cartesian product",
            )
    }

    @Nested
    inner class RegexRecompilationNegatives {
        @Test
        fun `string args in loop`() =
            assertNoFindings(
                "regex-recompilation-in-loop/negative/string-args-in-loop.js",
                "regex-recompilation-in-loop",
                "string.replace() with string args is not regex in JavaScript",
            )
    }

    @Nested
    inner class InLoopCollectionBuildingNegatives {
        @Test
        fun `toLowerCase in loop`() =
            assertNoFindings(
                "in-loop-collection-building/negative/to-lower-case-in-loop.js",
                "in-loop-collection-building",
                "String.toLowerCase() is not a collection building operation",
            )
    }

    @Nested
    inner class IOInLoopNegatives {
        @Test
        fun `fetch outside loop`() =
            assertNoFindings(
                "io-in-loop/negative/fetch-outside-loop.js",
                "io-in-loop",
                "fetch() outside loop should not fire",
            )
    }

    @Nested
    inner class LoopInvariantHoistingNegatives {
        @Test
        fun `depends on item`() =
            assertNoFindings(
                "loop-invariant-hoisting/negative/depends-on-item.js",
                "loop-invariant-hoisting",
                "expression depends on loop variable, cannot be hoisted",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationNegatives {
        @Test
        fun `different sources`() =
            assertNoFindings(
                "repeated-collection-iteration/negative/different-sources.js",
                "repeated-collection-iteration",
                "arrays with different sources are not fusible",
            )
    }

    @Nested
    inner class NestedLookupNegatives {
        @Test
        fun `Map has in loop`() =
            assertNoFindings(
                "nested-lookup/negative/map-has-in-loop.js",
                "nested-lookup",
                "Map.has() is O(1), not a linear lookup",
            )

        @Test
        fun `Set has in loop`() =
            assertNoFindings(
                "nested-lookup/negative/set-has-in-loop.js",
                "nested-lookup",
                "Set.has() is O(1), not a linear lookup",
            )

        @Test
        fun `TypeScript typed Map in loop`() =
            assertNoFindings(
                "nested-lookup/negative/typed-map-in-loop.ts",
                "nested-lookup",
                "TypeScript Map<K,V> parameter is O(1), suppressed by type inference",
            )

        @Test
        fun `TypeScript typed Set in loop`() =
            assertNoFindings(
                "nested-lookup/negative/typed-set-in-loop.ts",
                "nested-lookup",
                "TypeScript Set<T> parameter is O(1), suppressed by type inference",
            )
    }

    // ── Regression fixtures: specific FP patterns from benchmark repos ────────

    @Nested
    inner class QuadraticRemovalRegressions {
        @Test
        fun `Map delete in loop is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/map-delete-in-loop.ts",
                "quadratic-removal",
                "Map.delete() is O(1), not an array shift",
            )
    }

    @Nested
    inner class NestedLookupRegressions {
        @Test
        fun `Set has is O(1)`() =
            assertNoFindings(
                "nested-lookup/regression/set-has-in-loop.ts",
                "nested-lookup",
                "Set.has() is O(1), not a linear lookup",
            )

        @Test
        fun `string includes is not collection operation`() =
            assertNoFindings(
                "nested-lookup/regression/string-includes.ts",
                "nested-lookup",
                "String.includes() is a string method, not Array.includes()",
            )
    }

    // ── Positive fixtures: TP anchors ──────────────────────────────────────

    @Nested
    inner class CardinalityExplosionAnchors {
        @Test
        fun `Cartesian product`() =
            assertHasFindings(
                "cardinality-explosion/positive/cartesian-product.js",
                "cardinality-explosion",
                "Cartesian product should always be detected",
            )
    }

    @Nested
    inner class NestedLookupAnchors {
        @Test
        fun `array includes in loop`() =
            assertHasFindings(
                "nested-lookup/positive/array-includes-in-loop.js",
                "nested-lookup",
                "Array.includes() in for-of loop is the canonical JS nested lookup",
            )
    }

    @Nested
    inner class IOInLoopAnchors {
        @Test
        fun `fetch in loop`() =
            assertHasFindings(
                "io-in-loop/positive/fetch-in-loop.js",
                "io-in-loop",
                "fetch() in loop is IO in loop",
            )
    }

    @Nested
    inner class InLoopCollectionBuildingAnchors {
        @Test
        fun `concat in loop`() =
            assertHasFindings(
                "in-loop-collection-building/positive/concat-in-loop.js",
                "in-loop-collection-building",
                "Array.concat() in loop builds a new array each iteration",
            )
    }

    @Nested
    inner class RegexRecompilationAnchors {
        @Test
        fun `regex in loop`() =
            assertHasFindings(
                "regex-recompilation-in-loop/positive/regex-in-loop.js",
                "regex-recompilation-in-loop",
                "regex literal in loop should be detected",
            )
    }

    @Nested
    inner class LoopInvariantHoistingAnchors {
        @Test
        fun `config in loop`() =
            assertHasFindings(
                "loop-invariant-hoisting/positive/config-in-loop.js",
                "loop-invariant-hoisting",
                "loop-invariant config lookup should be hoisted",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationAnchors {
        @Test
        fun `two forEach loops`() =
            assertHasFindings(
                "repeated-collection-iteration/positive/two-foreach-loops.js",
                "repeated-collection-iteration",
                "two forEach passes over same array should fuse",
            )
    }
}
