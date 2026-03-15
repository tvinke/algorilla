package com.github.tvinke.algorilla.lang.groovy.parser

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Full-pipeline precision regression tests for Groovy fixtures.
 *
 * Catches regressions from TypeEnvironment, YAML semantics, and cross-rule interactions.
 * Groovy lacks type declarations, so confidence demotion plays a bigger role here.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class CardinalityExplosionNegatives {
        @Test
        fun `single loop`() =
            assertNoFindings(
                "cardinality-explosion/negative/single-loop.groovy",
                "cardinality-explosion",
                "single loop iteration is not a Cartesian product",
            )
    }

    @Nested
    inner class ExpensiveConstructionNegatives {
        @Test
        fun `no heavyweight types`() =
            assertNoFindings(
                "expensive-construction/negative/no-heavyweight-types.groovy",
                "heavyweight-object-per-invocation",
                "no heavyweight objects created",
            )
    }

    @Nested
    inner class HiddenNestedLoopNegatives {
        @Test
        fun `each calls method without loop`() =
            assertNoFindings(
                "hidden-nested-loop/negative/each-calls-method-without-loop.groovy",
                "hidden-nested-loop",
                "called method has no loop body",
            )
    }

    @Nested
    inner class InLoopCollectionBuildingNegatives {
        @Test
        fun `single add in each`() =
            assertNoFindings(
                "in-loop-collection-building/negative/single-add-in-each.groovy",
                "in-loop-collection-building",
                "single element add is normal iteration",
            )
    }

    @Nested
    inner class LazyLoadingNegatives {
        @Test
        fun `scalar getter`() =
            assertNoFindings(
                "lazy-loading-in-loop/negative/scalar-getter.groovy",
                "lazy-loading-in-loop",
                "scalar getter is not a lazy-loaded collection",
            )
    }

    @Nested
    inner class LoopInvariantHoistingNegatives {
        @Test
        fun `depends on item`() =
            assertNoFindings(
                "loop-invariant-hoisting/negative/depends-on-item.groovy",
                "loop-invariant-hoisting",
                "expression depends on loop variable, cannot be hoisted",
            )

        @Test
        fun `cast argument depends on loop variable`() =
            assertNoFindings(
                "loop-invariant-hoisting/negative/cast-arg-depends-on-loop.groovy",
                "loop-invariant-hoisting",
                "normalizeMap((Map) child) depends on 'child' from the loop",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationNegatives {
        @Test
        fun `different sources`() =
            assertNoFindings(
                "repeated-collection-iteration/negative/different-sources.groovy",
                "repeated-collection-iteration",
                "collections with different sources are not fusible",
            )
    }

    @Nested
    inner class NestedLookupNegatives {
        @Test
        fun `set contains in each`() =
            assertNoFindings(
                "nested-lookup/negative/set-contains-in-each.groovy",
                "nested-lookup",
                "Set.contains() is O(1), not a linear lookup",
            )
    }

    @Nested
    inner class RedundantExpensiveCallNegatives {
        @Test
        fun `different args`() =
            assertNoFindings(
                "redundant-expensive-call/negative/different-args.groovy",
                "redundant-expensive-call",
                "different arguments are not redundant",
            )
    }

    @Nested
    inner class RepeatedLinearScanNegatives {
        @Test
        fun `single groupby`() =
            assertNoFindings(
                "repeated-linear-scan/negative/single-groupby.groovy",
                "repeated-linear-scan",
                "single groupBy is not repeated",
            )
    }

    @Nested
    inner class UnmemoizedRecursionNegatives {
        @Test
        fun `tree traversal`() =
            assertNoFindings(
                "unmemoized-recursion/negative/tree-traversal.groovy",
                "unmemoized-recursion",
                "tree traversal visits different nodes, not unmemoized",
            )
    }

    // ── Regression fixtures: specific FP patterns from benchmark repos ────────

    @Nested
    inner class QuadraticRemovalRegressions {
        @Test
        fun `Iterator remove in while-loop is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/iterator-remove.groovy",
                "quadratic-removal",
                "Iterator.remove() is O(1) for the current position",
            )

        @Test
        fun `Map remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/map-remove.groovy",
                "quadratic-removal",
                "Map.remove() is O(1), hash-based removal",
            )
    }

    @Nested
    inner class NestedLookupRegressions {
        @Test
        fun `String contains is not collection lookup`() =
            assertNoFindings(
                "nested-lookup/regression/string-contains.groovy",
                "nested-lookup",
                "String.contains() is a string operation, not a collection lookup",
            )
    }

    // ── Positive fixtures: TP anchors ──────────────────────────────────────

    @Nested
    inner class CardinalityExplosionAnchors {
        @Test
        fun `Cartesian product`() =
            assertHasFindings(
                "cardinality-explosion/positive/cartesian-product.groovy",
                "cardinality-explosion",
                "Cartesian product should always be detected",
            )
    }

    @Nested
    inner class NestedLookupAnchors {
        @Test
        fun `list contains in each`() =
            assertHasFindings(
                "nested-lookup/positive/list-contains-in-each.groovy",
                "nested-lookup",
                "list.contains() in each loop is the canonical nested lookup",
            )
    }

    @Nested
    inner class HiddenNestedLoopAnchors {
        @Test
        fun `each calls method with loop`() =
            assertHasFindings(
                "hidden-nested-loop/positive/each-calls-method-with-loop.groovy",
                "hidden-nested-loop",
                "method called from each contains its own loop",
            )
    }

    @Nested
    inner class RedundantExpensiveCallAnchors {
        @Test
        fun `same args`() =
            assertHasFindings(
                "redundant-expensive-call/positive/same-args.groovy",
                "redundant-expensive-call",
                "identical expensive call with same args should be detected",
            )
    }

    @Nested
    inner class UnmemoizedRecursionAnchors {
        @Test
        fun `fibonacci`() =
            assertHasFindings(
                "unmemoized-recursion/positive/fibonacci.groovy",
                "unmemoized-recursion",
                "unmemoized fibonacci is exponential",
            )
    }

    @Nested
    inner class RepeatedLinearScanAnchors {
        @Test
        fun `repeated groupBy collectEntries`() =
            assertHasFindings(
                "repeated-linear-scan/positive/repeated-groupby-collectentries.groovy",
                "repeated-linear-scan",
                "repeated groupBy on same collection should fuse",
            )
    }

    @Nested
    inner class LoopInvariantHoistingAnchors {
        @Test
        fun `config in loop`() =
            assertHasFindings(
                "loop-invariant-hoisting/positive/config-in-loop.groovy",
                "loop-invariant-hoisting",
                "loop-invariant config lookup should be hoisted",
            )
    }
}
