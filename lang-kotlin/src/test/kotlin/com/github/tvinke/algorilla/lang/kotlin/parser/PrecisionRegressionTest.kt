package com.github.tvinke.algorilla.lang.kotlin.parser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Full-pipeline precision regression tests for Kotlin fixtures.
 *
 * Catches regressions from TypeEnvironment, YAML semantics, and cross-rule interactions
 * that single-rule tests miss. Cross-language counterpart to the Java regression suite.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class CardinalityExplosionNegatives {
        @Test
        fun `single loop`() =
            assertNoFindings(
                "cardinality-explosion/negative/single-loop.kt",
                "cardinality-explosion",
                "single loop iteration is not a Cartesian product",
            )
    }

    @Nested
    inner class ExpensiveConstructionNegatives {
        @Test
        fun `no heavyweight types`() =
            assertNoFindings(
                "expensive-construction/negative/no-heavyweight-types.kt",
                "heavyweight-object-per-invocation",
                "no heavyweight objects created",
            )
    }

    @Nested
    inner class HiddenNestedLoopNegatives {
        @Test
        fun `loop calls method without loop`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-method-without-loop.kt",
                "hidden-nested-loop",
                "called method has no loop body",
            )
    }

    @Nested
    inner class InLoopCollectionBuildingNegatives {
        @Test
        fun `single add in loop`() =
            assertNoFindings(
                "in-loop-collection-building/negative/single-add-in-loop.kt",
                "in-loop-collection-building",
                "single element add is normal iteration",
            )
    }

    @Nested
    inner class LazyLoadingNegatives {
        @Test
        fun `scalar getter`() =
            assertNoFindings(
                "lazy-loading-in-loop/negative/scalar-getter.kt",
                "lazy-loading-in-loop",
                "scalar property access is not lazy-loaded collection",
            )
    }

    @Nested
    inner class LoopInvariantHoistingNegatives {
        @Test
        fun `depends on item`() =
            assertNoFindings(
                "loop-invariant-hoisting/negative/depends-on-item.kt",
                "loop-invariant-hoisting",
                "expression depends on loop variable, cannot be hoisted",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationNegatives {
        @Test
        fun `different sources`() =
            assertNoFindings(
                "repeated-collection-iteration/negative/different-sources.kt",
                "repeated-collection-iteration",
                "sequences on different sources are not fusible",
            )
    }

    @Nested
    inner class NestedLookupNegatives {
        @Test
        fun `set contains in loop`() =
            assertNoFindings(
                "nested-lookup/negative/set-contains-in-loop.kt",
                "nested-lookup",
                "Set.contains() is O(1), not a linear lookup",
            )
    }

    @Nested
    inner class RedundantExpensiveCallNegatives {
        @Test
        fun `different args`() =
            assertNoFindings(
                "redundant-expensive-call/negative/different-args.kt",
                "redundant-expensive-call",
                "different arguments are not redundant",
            )
    }

    @Nested
    inner class UnmemoizedRecursionNegatives {
        @Test
        fun `tree traversal`() =
            assertNoFindings(
                "unmemoized-recursion/negative/tree-traversal.kt",
                "unmemoized-recursion",
                "tree traversal visits different nodes, not unmemoized",
            )
    }

    // ── Regression fixtures: specific FP patterns from benchmark repos ────────

    @Nested
    inner class QuadraticRemovalRegressions {
        @Test
        fun `mutableMapOf remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/mutablemap-remove.kt",
                "quadratic-removal",
                "mutableMapOf() backed by HashMap — remove is O(1)",
            )

        @Test
        fun `ConcurrentHashMap field remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/concurrent-field-remove.kt",
                "quadratic-removal",
                "ConcurrentHashMap.remove() is O(1), cross-language TypeEnvironment check",
            )
    }

    @Nested
    inner class NestedLookupRegressions {
        @Test
        fun `String contains is not collection lookup`() =
            assertNoFindings(
                "nested-lookup/regression/string-contains-in-loop.kt",
                "nested-lookup",
                "String.contains() is a string operation, not a collection lookup",
            )
    }

    @Nested
    inner class HiddenNestedLoopRegressions {
        @Test
        fun `tree walk recursive is not hidden nesting`() =
            assertNoFindings(
                "hidden-nested-loop/regression/tree-walk-recursive.kt",
                "hidden-nested-loop",
                "recursive tree traversal is intentional, not accidental nesting",
            )
    }

    // ── Positive fixtures: TP anchors ──────────────────────────────────────

    @Nested
    inner class CardinalityExplosionAnchors {
        @Test
        fun `Cartesian product`() =
            assertHasFindings(
                "cardinality-explosion/positive/cartesian-product.kt",
                "cardinality-explosion",
                "Cartesian product should always be detected",
            )
    }

    @Nested
    inner class NestedLookupAnchors {
        @Test
        fun `list contains in for`() =
            assertHasFindings(
                "nested-lookup/positive/list-contains-in-for.kt",
                "nested-lookup",
                "list.contains() in for-loop is the canonical nested lookup",
            )
    }

    @Nested
    inner class HiddenNestedLoopAnchors {
        @Test
        fun `loop calls method with loop`() =
            assertHasFindings(
                "hidden-nested-loop/positive/loop-calls-method-with-loop.kt",
                "hidden-nested-loop",
                "method called from loop contains its own loop",
            )
    }

    @Nested
    inner class RedundantExpensiveCallAnchors {
        @Test
        fun `same args`() =
            assertHasFindings(
                "redundant-expensive-call/positive/same-args.kt",
                "redundant-expensive-call",
                "identical expensive call with same args should be detected",
            )
    }

    @Nested
    inner class UnmemoizedRecursionAnchors {
        @Test
        fun `fibonacci`() =
            assertHasFindings(
                "unmemoized-recursion/positive/fibonacci.kt",
                "unmemoized-recursion",
                "unmemoized fibonacci is exponential",
            )
    }

    @Nested
    inner class LoopInvariantHoistingAnchors {
        @Test
        fun `config in loop`() =
            assertHasFindings(
                "loop-invariant-hoisting/positive/config-in-loop.kt",
                "loop-invariant-hoisting",
                "loop-invariant config lookup should be hoisted",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationAnchors {
        @Test
        fun `two sequences`() =
            assertHasFindings(
                "repeated-collection-iteration/positive/two-sequences.kt",
                "repeated-collection-iteration",
                "two sequence passes over same source should fuse",
            )
    }
}
