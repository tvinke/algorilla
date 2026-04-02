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
 * Every positive fixture is run as a TP anchor to catch false-negative regressions.
 */
internal class PrecisionRegressionTest : FullPipelineTestSupport() {
    // ── Negative fixtures: must NOT produce findings for their rule ──────────

    @Nested
    inner class CardinalityExplosionNegatives {
        @Test
        fun `enum values iteration`() =
            assertNoFindings(
                "cardinality-explosion/negative/enum-values-iteration.java",
                "cardinality-explosion",
                "enum values() produces a bounded array, not a Cartesian product",
            )

        @Test
        fun `map entry unpacking`() =
            assertNoFindings(
                "cardinality-explosion/negative/map-entry-unpacking.java",
                "cardinality-explosion",
                "iterating map entries is single-pass, not Cartesian",
            )

        @Test
        fun `parent-child iteration`() =
            assertNoFindings(
                "cardinality-explosion/negative/parent-child-iteration.java",
                "cardinality-explosion",
                "parent-child is hierarchical, not Cartesian",
            )

        @Test
        fun `same collection nesting`() =
            assertNoFindings(
                "cardinality-explosion/negative/same-collection-nesting.java",
                "cardinality-explosion",
                "nested iteration over the same collection is intentional (e.g. pairwise comparison)",
            )

        @Test
        fun `single loop mutation`() =
            assertNoFindings(
                "cardinality-explosion/negative/single-loop-mutation.java",
                "cardinality-explosion",
                "mutation inside a single loop is not a Cartesian product",
            )
    }

    @Nested
    inner class ChainedGettersNegatives {
        @Test
        fun `chain without linear lookup`() =
            assertNoFindings(
                "chained-getters/negative/chain-without-linear-lookup.java",
                "chained-getters",
                "getter chain without linear lookup should not fire",
            )

        @Test
        fun `single getter call`() =
            assertNoFindings(
                "chained-getters/negative/single-getter-call.java",
                "chained-getters",
                "single getter is not a chain",
            )
    }

    @Nested
    inner class DateInSortNegatives {
        @Test
        fun `no date in comparator`() =
            assertNoFindings(
                "date-in-sort/negative/no-date-in-comparator.java",
                "expensive-sort-comparator",
                "comparator without date creation should not fire",
            )
    }

    @Nested
    inner class ExpensiveCallbackNegatives {
        @Test
        fun `simple callback`() =
            assertNoFindings(
                "expensive-callback/negative/simple-callback.java",
                "expensive-callback",
                "trivial callback should not fire",
            )
    }

    @Nested
    inner class ExpensiveSerializationNegatives {
        @Test
        fun `non-serialization in loop`() =
            assertNoFindings(
                "expensive-serialization-in-loop/negative/non-serialization-in-loop.java",
                "expensive-serialization-in-loop",
                "non-serialization method in loop should not fire",
            )

        @Test
        fun `serialization outside loop`() =
            assertNoFindings(
                "expensive-serialization-in-loop/negative/serialization-outside-loop.java",
                "expensive-serialization-in-loop",
                "serialization outside a loop should not fire",
            )
    }

    @Nested
    inner class ExpensiveSortComparatorNegatives {
        @Test
        fun `simple comparator`() =
            assertNoFindings(
                "expensive-sort-comparator/negative/simple-comparator.java",
                "expensive-sort-comparator",
                "simple field comparison is not expensive",
            )
    }

    @Nested
    inner class FilterAfterSortNegatives {
        @Test
        fun `filter then sort`() =
            assertNoFindings(
                "filter-after-sort/negative/filter-then-sort.java",
                "filter-after-sort",
                "filter-before-sort is the correct order",
            )

        @Test
        fun `separate chains with if`() =
            assertNoFindings(
                "filter-after-sort/negative/separate-chains-with-if.java",
                "filter-after-sort",
                "sort and filter on separate chains in different branches",
            )

        @Test
        fun `separate chains`() =
            assertNoFindings(
                "filter-after-sort/negative/separate-chains.java",
                "filter-after-sort",
                "sort and filter on separate stream chains",
            )
    }

    @Nested
    inner class FullScanForSingleLookupNegatives {
        @Test
        fun `no bulk load`() =
            assertNoFindings(
                "full-scan-for-single-lookup/negative/no-bulk-load.java",
                "full-scan-for-single-lookup",
                "no bulk-load-then-filter pattern present",
            )
    }

    @Nested
    inner class HeavyweightObjectNegatives {
        @Test
        fun `no heavyweight types`() =
            assertNoFindings(
                "heavyweight-object-per-invocation/negative/no-heavyweight-types.java",
                "heavyweight-object-per-invocation",
                "no heavyweight objects created in method",
            )
    }

    @Nested
    inner class HiddenNestedLoopNegatives {
        @Test
        fun `loop calls collection copy method`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-collection-copy-method.java",
                "hidden-nested-loop",
                "collection copy is a recognized safe pattern",
            )

        @Test
        fun `loop calls method on different receiver`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-method-on-different-receiver.java",
                "hidden-nested-loop",
                "method on different receiver is not a hidden nested loop",
            )

        @Test
        fun `loop calls method with only conditionals`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-method-with-only-conditionals.java",
                "hidden-nested-loop",
                "branching without loops is not hidden nesting",
            )

        @Test
        fun `loop calls method without loop`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-method-without-loop.java",
                "hidden-nested-loop",
                "called method has no loop body",
            )

        @Test
        fun `loop calls overloaded method`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-overloaded-method.java",
                "hidden-nested-loop",
                "overloaded method without loop should not fire",
            )

        @Test
        fun `loop calls string utility method`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-string-utility-method.java",
                "hidden-nested-loop",
                "string utility methods are trivial, not hidden loops",
            )

        @Test
        fun `loop calls unresolvable method`() =
            assertNoFindings(
                "hidden-nested-loop/negative/loop-calls-unresolvable-method.java",
                "hidden-nested-loop",
                "unresolvable method should not be assumed to contain a loop",
            )

        @Test
        fun `mixed calls only some have loops`() =
            assertNoFindings(
                "hidden-nested-loop/negative/mixed-calls-only-some-have-loops.java",
                "hidden-nested-loop",
                "only some calls have loops; the one without should not trigger",
            )

        @Test
        fun `no loop context`() =
            assertNoFindings(
                "hidden-nested-loop/negative/no-loop-context.java",
                "hidden-nested-loop",
                "no enclosing loop context",
            )

        @Test
        fun `recursive method`() =
            assertNoFindings(
                "hidden-nested-loop/negative/recursive-method.java",
                "hidden-nested-loop",
                "recursion is not hidden nested iteration",
            )
    }

    @Nested
    inner class RegexRecompilationNegatives {
        @Test
        fun `map replace all`() =
            assertNoFindings(
                "regex-recompilation-in-loop/negative/map-replace-all.java",
                "regex-recompilation-in-loop",
                "Map.replaceAll() is not a regex operation",
            )

        @Test
        fun `no regex methods`() =
            assertNoFindings(
                "regex-recompilation-in-loop/negative/no-regex-methods.java",
                "regex-recompilation-in-loop",
                "no regex methods present",
            )

        @Test
        fun `single char split`() =
            assertNoFindings(
                "regex-recompilation-in-loop/negative/single-char-split.java",
                "regex-recompilation-in-loop",
                "single-char split is optimized by the JVM, not a regex",
            )
    }

    @Nested
    inner class InLoopCollectionBuildingNegatives {
        @Test
        fun `add all outside loop`() =
            assertNoFindings(
                "in-loop-collection-building/negative/add-all-outside-loop.java",
                "in-loop-collection-building",
                "addAll outside loop body should not fire",
            )

        @Test
        fun `single add in loop`() =
            assertNoFindings(
                "in-loop-collection-building/negative/single-add-in-loop.java",
                "in-loop-collection-building",
                "single element add is normal iteration, not bulk copying",
            )

        @Test
        fun `toLowerCase in loop`() =
            assertNoFindings(
                "in-loop-collection-building/negative/to-lower-case-in-loop.java",
                "in-loop-collection-building",
                "String.toLowerCase() is not a collection building operation",
            )
    }

    @Nested
    inner class IOInLoopNegatives {
        @Test
        fun `in-memory buffer write`() =
            assertNoFindings(
                "io-in-loop/negative/in-memory-buffer-write.java",
                "io-in-loop",
                "in-memory buffer (ByteArrayOutputStream, StringWriter) is not real IO",
            )

        @Test
        fun `jdbc outside loop`() =
            assertNoFindings(
                "io-in-loop/negative/jdbc-outside-loop.java",
                "io-in-loop",
                "JDBC call outside loop should not fire",
            )
    }

    @Nested
    inner class LazyLoadingNegatives {
        @Test
        fun `scalar getter in loop`() =
            assertNoFindings(
                "lazy-loading-in-loop/negative/scalar-getter-in-loop.java",
                "lazy-loading-in-loop",
                "scalar getter (e.g. getName()) is not a lazy-loaded collection",
            )
    }

    @Nested
    inner class LoopInvariantHoistingNegatives {
        @Test
        fun `depends on loop var`() =
            assertNoFindings(
                "loop-invariant-hoisting/negative/depends-on-loop-var.java",
                "loop-invariant-hoisting",
                "expression depends on loop variable, cannot be hoisted",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationNegatives {
        @Test
        fun `branched streams`() =
            assertNoFindings(
                "repeated-collection-iteration/negative/branched-streams.java",
                "repeated-collection-iteration",
                "streams in different branches are not redundant passes",
            )

        @Test
        fun `different sources`() =
            assertNoFindings(
                "repeated-collection-iteration/negative/different-sources.java",
                "repeated-collection-iteration",
                "streams on different sources are not fusible",
            )
    }

    @Nested
    inner class NPlusOneQueryNegatives {
        @Test
        fun `batch patterns excluded`() =
            assertNoFindings(
                "n-plus-one-query/negative/batch-patterns-excluded.java",
                "n-plus-one-query",
                "batch fetch/saveAll patterns are not N+1",
            )

        @Test
        fun `cache target excluded`() =
            assertNoFindings(
                "n-plus-one-query/negative/cache-target-excluded.java",
                "n-plus-one-query",
                "calls to cache targets should be excluded",
            )
    }

    @Nested
    inner class NestedLookupNegatives {
        @Test
        fun `cache map target in loop`() =
            assertNoFindings(
                "nested-lookup/negative/cache-map-target-in-loop.java",
                "nested-lookup",
                "cache/map.get() is O(1), not a linear lookup",
            )

        @Test
        fun `no loop`() =
            assertNoFindings(
                "nested-lookup/negative/no-loop.java",
                "nested-lookup",
                "no enclosing loop — no nested lookup",
            )

        @Test
        fun `set contains in loop`() =
            assertNoFindings(
                "nested-lookup/negative/set-contains-in-loop.java",
                "nested-lookup",
                "Set.contains() is O(1), not a linear lookup",
            )

        @Test
        fun `set field contains in loop`() =
            assertNoFindings(
                "nested-lookup/negative/set-field-contains-in-loop.java",
                "nested-lookup",
                "Set field .contains() is O(1), suppressed by TypeEnvironment",
            )

        @Test
        fun `string contains in loop`() =
            assertNoFindings(
                "nested-lookup/negative/string-contains-in-loop.java",
                "nested-lookup",
                "String.contains() is a string op, not a collection lookup",
            )

        @Test
        fun `string field contains in loop`() =
            assertNoFindings(
                "nested-lookup/negative/string-field-contains-in-loop.java",
                "nested-lookup",
                "String field .contains() is a string op, not a collection lookup",
            )
    }

    @Nested
    inner class ParallelPipelineNegatives {
        @Test
        fun `parallel collect safe`() =
            assertNoFindings(
                "parallel-pipeline-bottleneck/negative/parallel-collect-safe.java",
                "parallel-pipeline-bottleneck",
                "thread-safe collector in parallel stream is safe",
            )

        @Test
        fun `sequential stream add`() =
            assertNoFindings(
                "parallel-pipeline-bottleneck/negative/sequential-stream-add.java",
                "parallel-pipeline-bottleneck",
                "sequential stream with shared mutable state is fine",
            )
    }

    @Nested
    inner class QuadraticRemovalNegatives {
        @Test
        fun `files delete in loop`() =
            assertNoFindings(
                "quadratic-removal/negative/files-delete-in-loop.java",
                "quadratic-removal",
                "File.delete() is IO, not list removal",
            )

        @Test
        fun `map remove`() =
            assertNoFindings(
                "quadratic-removal/negative/map-remove.java",
                "quadratic-removal",
                "Map.remove() is O(1), not quadratic",
            )

        @Test
        fun `typed map remove`() =
            assertNoFindings(
                "quadratic-removal/negative/typed-map-remove.java",
                "quadratic-removal",
                "HashMap/ConcurrentHashMap.remove() is O(1), suppressed by TypeEnvironment",
            )
    }

    @Nested
    inner class RedundantExpensiveCallNegatives {
        @Test
        fun `chained method target`() =
            assertNoFindings(
                "redundant-expensive-call/negative/chained-method-target.java",
                "redundant-expensive-call",
                "chained method calls have different targets",
            )

        @Test
        fun `different args method chain`() =
            assertNoFindings(
                "redundant-expensive-call/negative/different-args-method-chain.java",
                "redundant-expensive-call",
                "different arguments mean different computations",
            )

        @Test
        fun `different enum args`() =
            assertNoFindings(
                "redundant-expensive-call/negative/different-enum-args.java",
                "redundant-expensive-call",
                "different enum arguments are semantically different",
            )

        @Test
        fun `different method call args`() =
            assertNoFindings(
                "redundant-expensive-call/negative/different-method-call-args.java",
                "redundant-expensive-call",
                "different method call arguments are not redundant",
            )

        @Test
        fun `different string args`() =
            assertNoFindings(
                "redundant-expensive-call/negative/different-string-args.java",
                "redundant-expensive-call",
                "different string arguments are not redundant",
            )

        @Test
        fun `real world replaceAll`() =
            assertNoFindings(
                "redundant-expensive-call/negative/real-world-replaceall.java",
                "redundant-expensive-call",
                "replaceAll with different patterns is not redundant",
            )

        @Test
        fun `side effect disable`() =
            assertNoFindings(
                "redundant-expensive-call/negative/side-effect-disable.java",
                "redundant-expensive-call",
                "calls with side effects may need repetition",
            )
    }

    @Nested
    inner class RepeatedLinearScanNegatives {
        @Test
        fun `set field contains`() =
            assertNoFindings(
                "repeated-linear-scan/negative/set-field-contains.java",
                "repeated-linear-scan",
                "Set.contains() is O(1), not a linear scan",
            )

        @Test
        fun `single groupBy`() =
            assertNoFindings(
                "repeated-linear-scan/negative/single-groupby.java",
                "repeated-linear-scan",
                "single groupBy is not repeated",
            )

        @Test
        fun `single lookup`() =
            assertNoFindings(
                "repeated-linear-scan/negative/single-lookup.java",
                "repeated-linear-scan",
                "single lookup is not repeated",
            )

        @Test
        fun `string field indexOf`() =
            assertNoFindings(
                "repeated-linear-scan/negative/string-field-index-of.java",
                "repeated-linear-scan",
                "String.indexOf() is a string op, not a collection scan",
            )

        @Test
        fun `string indexOf`() =
            assertNoFindings(
                "repeated-linear-scan/negative/string-index-of.java",
                "repeated-linear-scan",
                "String.indexOf() is a string op, not a collection scan",
            )
    }

    @Nested
    inner class RepeatedReflectionNegatives {
        @Test
        fun `no reflection`() =
            assertNoFindings(
                "repeated-reflection-in-loop/negative/no-reflection.java",
                "repeated-reflection-in-loop",
                "no reflection calls present",
            )
    }

    @Nested
    inner class RepeatedRegexNegatives {
        @Test
        fun `pattern compile with variable arg`() =
            assertNoFindings(
                "repeated-regex-in-loop/negative/pattern-compile-with-variable-arg.java",
                "repeated-regex-in-loop",
                "Pattern.compile with variable argument may need recompilation",
            )

        @Test
        fun `pattern compiled outside loop`() =
            assertNoFindings(
                "repeated-regex-in-loop/negative/pattern-compiled-outside-loop.java",
                "repeated-regex-in-loop",
                "pattern compiled outside loop is correctly hoisted",
            )
    }

    @Nested
    inner class SequentialAsyncJoinNegatives {
        @Test
        fun `futures collected then joined`() =
            assertNoFindings(
                "sequential-async-join-in-loop/negative/futures-collected-then-joined.java",
                "sequential-async-join-in-loop",
                "collecting futures then joining is the correct parallel pattern",
            )

        @Test
        fun `join on non-future variable`() =
            assertNoFindings(
                "sequential-async-join-in-loop/negative/join-on-non-future-variable.java",
                "sequential-async-join-in-loop",
                "join() on a non-Future type is not async",
            )
    }

    @Nested
    inner class SortForLastNegatives {
        @Test
        fun `sort then iterate`() =
            assertNoFindings(
                "sort-for-last/negative/sort-then-iterate.java",
                "sort-for-last",
                "sort followed by full iteration is legitimate",
            )

        @Test
        fun `sort and access on different collections`() =
            assertNoFindings(
                "sort-for-last/negative/different-collection-sort-and-access.java",
                "sort-for-last",
                "Sorting items but accessing indices.get(0) — different collections",
            )
    }

    @Nested
    inner class StringConcatNegatives {
        @Test
        fun `no concat`() =
            assertNoFindings(
                "string-concat-in-loop/negative/no-concat.java",
                "string-concat-in-loop",
                "no string concatenation in loop",
            )
    }

    @Nested
    inner class UncachedGetterNegatives {
        @Test
        fun `excluded generic get`() =
            assertNoFindings(
                "uncached-getter/negative/excluded-generic-get.java",
                "uncached-getter",
                "generic get() calls (Map.get, List.get) should be excluded",
            )

        @Test
        fun `getter called once`() =
            assertNoFindings(
                "uncached-getter/negative/getter-called-once.java",
                "uncached-getter",
                "single getter call is not repeated",
            )

        @Test
        fun `getter with different args`() =
            assertNoFindings(
                "uncached-getter/negative/getter-with-different-args.java",
                "uncached-getter",
                "getter calls with different arguments are not redundant",
            )
    }

    @Nested
    inner class UnmemoizedRecursionNegatives {
        @Test
        fun `memoized fibonacci`() =
            assertNoFindings(
                "unmemoized-recursion/negative/memoized-fibonacci.java",
                "unmemoized-recursion",
                "fibonacci with memoization cache should not fire",
            )

        @Test
        fun `tree traversal`() =
            assertNoFindings(
                "unmemoized-recursion/negative/tree-traversal.java",
                "unmemoized-recursion",
                "tree traversal visits different nodes, not unmemoized recursion",
            )
    }

    // ── Regression fixtures: specific FP patterns from benchmark repos ────────

    @Nested
    inner class QuadraticRemovalRegressions {
        @Test
        fun `HashMap via constructor remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/hashmap-constructor-remove.java",
                "quadratic-removal",
                "HashMap.remove() is O(1), suppressed by TypeEnvironment constructor inference",
            )

        @Test
        fun `ConcurrentHashMap field remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/concurrent-hashmap-field-remove.java",
                "quadratic-removal",
                "ConcurrentHashMap.remove() is O(1), suppressed by TypeEnvironment field type",
            )

        @Test
        fun `LinkedHashMap remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/linked-hashmap-remove.java",
                "quadratic-removal",
                "LinkedHashMap.remove() is O(1), hash-based removal",
            )

        @Test
        fun `Iterator remove is O(1)`() =
            assertNoFindings(
                "quadratic-removal/regression/iterator-remove.java",
                "quadratic-removal",
                "Iterator.remove() is O(1) for the current position",
            )
    }

    @Nested
    inner class NestedLookupRegressions {
        @Test
        fun `String contains with wildcard is not collection lookup`() =
            assertNoFindings(
                "nested-lookup/regression/string-wildcard-contains.java",
                "nested-lookup",
                "String.contains() is a string operation, not a collection lookup",
            )

        @Test
        fun `Arrays asList contains on small bounded list`() =
            assertNoFindings(
                "nested-lookup/regression/arrays-aslist-contains.java",
                "nested-lookup",
                "Arrays.asList() with constant elements is bounded, not a scalability issue",
            )

        @Test
        fun `Arrays asList assigned receiver contains on small bounded list`() =
            assertNoFindings(
                "nested-lookup/regression/arrays-aslist-assigned-contains.java",
                "nested-lookup",
                "Arrays.asList() assigned to a local variable is still bounded, not a scalability issue",
            )
    }

    @Nested
    inner class InLoopCollectionBuildingRegressions {
        @Test
        fun `putAll on config merge`() =
            assertNoFindings(
                "in-loop-collection-building/regression/putall-varargs-bundle.java",
                "in-loop-collection-building",
                "putAll for config merging is the intended accumulation pattern",
            )
    }

    @Nested
    inner class RedundantExpensiveCallRegressions {
        @Test
        fun `builder chain is not redundant`() =
            assertNoFindings(
                "redundant-expensive-call/regression/builder-chain-not-redundant.java",
                "redundant-expensive-call",
                "builder .append().append() chain calls have different arguments",
            )
    }

    // ── Positive fixtures: TP anchors (must still detect through full pipeline) ──

    @Nested
    inner class CardinalityExplosionAnchors {
        @Test
        fun `Cartesian product`() =
            assertHasFindings(
                "cardinality-explosion/positive/cartesian-product.java",
                "cardinality-explosion",
                "Cartesian product should always be detected",
            )

        @Test
        fun `flatmap explosion`() =
            assertHasFindings(
                "cardinality-explosion/positive/flatmap-explosion.java",
                "cardinality-explosion",
                "flatMap-based cardinality explosion should be detected",
            )
    }

    @Nested
    inner class NestedLookupAnchors {
        @Test
        fun `list contains in for`() =
            assertHasFindings(
                "nested-lookup/positive/list-contains-in-for.java",
                "nested-lookup",
                "list.contains() in for-loop is the canonical nested lookup",
            )

        @Test
        fun `list contains in while`() =
            assertHasFindings(
                "nested-lookup/positive/list-contains-in-while.java",
                "nested-lookup",
                "list.contains() in while-loop should be detected",
            )

        @Test
        fun `foreach indexOf`() =
            assertHasFindings(
                "nested-lookup/positive/foreach-indexOf.java",
                "nested-lookup",
                "forEach with indexOf is a nested lookup",
            )

        @Test
        fun `stream anyMatch`() =
            assertHasFindings(
                "nested-lookup/positive/stream-anyMatch.java",
                "nested-lookup",
                "stream.anyMatch() inside loop is a nested lookup",
            )

        @Test
        fun `list field contains in loop`() =
            assertHasFindings(
                "nested-lookup/positive/list-field-contains-in-loop.java",
                "nested-lookup",
                "List field .contains() in loop should fire",
            )
    }

    @Nested
    inner class QuadraticRemovalAnchors {
        @Test
        fun `remove in loop`() =
            assertHasFindings(
                "quadratic-removal/positive/remove-in-loop.java",
                "quadratic-removal",
                "ArrayList.remove() in loop is quadratic",
            )
    }

    @Nested
    inner class HiddenNestedLoopAnchors {
        @Test
        fun `loop calls method with loop`() =
            assertHasFindings(
                "hidden-nested-loop/positive/loop-calls-method-with-loop.java",
                "hidden-nested-loop",
                "method called from loop contains its own loop",
            )

        @Test
        fun `foreach calls method with loop`() =
            assertHasFindings(
                "hidden-nested-loop/positive/foreach-calls-method-with-loop.java",
                "hidden-nested-loop",
                "forEach calling method with loop is hidden nesting",
            )
    }

    @Nested
    inner class IOInLoopAnchors {
        @Test
        fun `jdbc in loop`() =
            assertHasFindings(
                "io-in-loop/positive/jdbc-in-loop.java",
                "io-in-loop",
                "JDBC executeQuery in loop is IO in loop",
            )

        @Test
        fun `spring rest in loop`() =
            assertHasFindings(
                "io-in-loop/positive/spring-rest-in-loop.java",
                "io-in-loop",
                "RestTemplate call in loop is IO in loop",
            )
    }

    @Nested
    inner class RedundantExpensiveCallAnchors {
        @Test
        fun `same method call args`() =
            assertHasFindings(
                "redundant-expensive-call/positive/same-method-call-args.java",
                "redundant-expensive-call",
                "identical expensive call with same method call args should be detected",
            )
    }

    @Nested
    inner class StringConcatAnchors {
        @Test
        fun `concat in loop`() =
            assertHasFindings(
                "string-concat-in-loop/positive/concat-in-loop.java",
                "string-concat-in-loop",
                "string concatenation in loop is quadratic",
            )
    }

    @Nested
    inner class UnmemoizedRecursionAnchors {
        @Test
        fun `fibonacci`() =
            assertHasFindings(
                "unmemoized-recursion/positive/fibonacci.java",
                "unmemoized-recursion",
                "unmemoized fibonacci is exponential",
            )
    }

    @Nested
    inner class RepeatedRegexAnchors {
        @Test
        fun `pattern compile in loop`() =
            assertHasFindings(
                "repeated-regex-in-loop/positive/pattern-compile-in-order-loop.java",
                "repeated-regex-in-loop",
                "Pattern.compile() in loop should be hoisted",
            )
    }

    @Nested
    inner class RegexRecompilationAnchors {
        @Test
        fun `matches in loop`() =
            assertHasFindings(
                "regex-recompilation-in-loop/positive/matches-in-loop.java",
                "regex-recompilation-in-loop",
                "String.matches() in loop compiles regex each iteration",
            )
    }

    @Nested
    inner class LoopInvariantHoistingAnchors {
        @Test
        fun `config in loop`() =
            assertHasFindings(
                "loop-invariant-hoisting/positive/config-in-loop.java",
                "loop-invariant-hoisting",
                "loop-invariant config lookup should be hoisted",
            )
    }

    @Nested
    inner class FilterAfterSortAnchors {
        @Test
        fun `sort then filter`() =
            assertHasFindings(
                "filter-after-sort/positive/sort-then-filter.java",
                "filter-after-sort",
                "sorting before filtering wastes work on discarded elements",
            )
    }

    @Nested
    inner class RepeatedCollectionIterationAnchors {
        @Test
        fun `two stream pipelines`() =
            assertHasFindings(
                "repeated-collection-iteration/positive/two-stream-pipelines.java",
                "repeated-collection-iteration",
                "two separate stream passes over same source should fuse",
            )
    }
}
