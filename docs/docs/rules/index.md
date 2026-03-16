# Rules Overview

Algorilla ships with 28 built-in rules that detect common algorithmic complexity anti-patterns, organized in six categories.

## Loop amplifiers

Expensive operations hidden inside loop bodies that multiply the cost per iteration.

| Rule | Impact |
|------|--------|
| [Nested Lookup](nested-lookup.md) | Linear search in a loop → quadratic cost. Fix: use a Set |
| [Repeated Linear Scan](repeated-linear-scan.md) | Same collection scanned multiple times → combine into one pass |
| [Repeated Regex in Loop](repeated-regex-in-loop.md) | Regex compiled every iteration → compile once before the loop |
| [Regex Recompilation in Loop](regex-recompilation-in-loop.md) | Regex literal recompiled per call → hoist to a constant |
| [Expensive Serialization in Loop](expensive-serialization-in-loop.md) | Serializer created per iteration → reuse a single instance |
| [Sequential Async Join in Loop](sequential-async-join-in-loop.md) | Awaits one-by-one in a loop → launch all, await together |
| [In-Loop Collection Building](in-loop-collection-building.md) | Collection copied every iteration → build once outside the loop |
| [String Concat in Loop](string-concat-in-loop.md) | String copied every iteration → use StringBuilder |
| [Quadratic Removal](quadratic-removal.md) | Removing from middle of a list shifts all elements → use an iterator or filter |
| [Repeated Reflection in Loop](repeated-reflection-in-loop.md) | Reflection lookup per iteration → cache the Method/Field |
| [Hidden Nested Loop](hidden-nested-loop.md) | Method call hides a nested loop → O(n²) invisible at call site |
| [IO In Loop](io-in-loop.md) | Network/DB/file call per iteration → batch or parallelize |
| [Expensive Callback](expensive-callback.md) | Expensive operation inside a callback (filter, map, sort) |
| [Cardinality Explosion](cardinality-explosion.md) | Cross-product output grows as O(n×m) |

## Sort abuse

Wasteful sort operations or expensive work inside sort comparators.

| Rule | Impact |
|------|--------|
| [Sort for Last](sort-for-last.md) | Sorting entire collection just to get min/max → use min()/max() |
| [Expensive Sort Comparator](expensive-sort-comparator.md) | Expensive computation inside comparator runs O(n log n) times → precompute |
| [Filter After Sort](filter-after-sort.md) | Sort runs before filter → filter first, sort fewer elements |

## Query patterns

Database and service call patterns that cause excessive I/O.

| Rule | Impact |
|------|--------|
| [N+1 Query](n-plus-one-query.md) | DB query per item in a loop → single bulk query |
| [Bulk Load for Single Lookup](bulk-load-for-single-lookup.md) | Loads all records to find one → query for just the one |
| [Lazy Loading In Loop](lazy-loading-in-loop.md) | ORM lazy-loads per iteration → eager fetch or batch |

## Construction cost

Unnecessary object creation overhead.

| Rule | Impact |
|------|--------|
| [Expensive Construction](expensive-construction.md) | Heavyweight object created per iteration → reuse a single instance |

## Redundancy

Repeated computations that could be cached.

| Rule | Impact |
|------|--------|
| [Redundant Expensive Call](redundant-expensive-call.md) | Same expensive call made multiple times → cache in a local variable |
| [Uncached Getter](uncached-getter.md) | Same getter called repeatedly → cache the result |
| [Chained Getters](chained-getters.md) | Deep getter chain traversed multiple times → flatten or cache |
| [Unmemoized Recursion](unmemoized-recursion.md) | Recursive function recomputes same inputs → add memoization |
| [Repeated Collection Iteration](repeated-collection-iteration.md) | Same collection iterated multiple times → combine into one pass |
| [Loop-Invariant Hoisting](loop-invariant-hoisting.md) | Same call repeated every iteration with same result → hoist before the loop |

## Concurrency

Patterns that undermine parallel execution performance.

| Rule | Impact |
|------|--------|
| [Parallel Pipeline Bottleneck](parallel-pipeline-bottleneck.md) | Synchronization bottleneck in parallel pipeline |

## Disabling Rules

In `.algorilla.yml`:

```yaml
rules:
  expensive-sort-comparator:
    enabled: false
```

Or suppress inline:

```java
// algorilla:ignore nested-lookup
for (Order order : orders) {
    if (discountedProductIds.contains(order.getProductId())) { ... }
}
```

Have questions about how rules work, what the confidence levels mean, or how Algorilla compares to other tools? Check the [FAQ](../faq.md).
