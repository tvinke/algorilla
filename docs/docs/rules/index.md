# Rules Overview

Algorilla ships with 23 built-in rules that detect common algorithmic complexity anti-patterns, organized in six categories.

## Loop amplifiers

Expensive operations hidden inside loop bodies that multiply the cost per iteration.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `nested-lookup` | [Nested Lookup](nested-lookup.md) | WARNING | O(n²) where O(n) suffices |
| `repeated-linear-scan` | [Repeated Linear Scan](repeated-linear-scan.md) | WARNING | O(n·k) where O(n) suffices |
| `repeated-regex-in-loop` | [Repeated Regex in Loop](repeated-regex-in-loop.md) | WARNING | O(n·compile) where O(n) suffices |
| `implicit-regex-in-loop` | [Implicit Regex in Loop](implicit-regex-in-loop.md) | WARNING | O(n·compile) where O(n) suffices |
| `expensive-serialization-in-loop` | [Expensive Serialization in Loop](expensive-serialization-in-loop.md) | WARNING | O(n·serialize) where O(n) suffices |
| `sequential-async-join-in-loop` | [Sequential Async Join in Loop](sequential-async-join-in-loop.md) | WARNING | O(n·wait) where O(max-wait) suffices |
| `in-loop-collection-building` | [In-Loop Collection Building](in-loop-collection-building.md) | WARNING | O(n·m) where O(n+m) suffices |
| `string-concat-in-loop` | [String Concat in Loop](string-concat-in-loop.md) | WARNING | O(n²) where O(n) suffices |
| `quadratic-removal` | [Quadratic Removal](quadratic-removal.md) | WARNING | O(n²) where O(n) suffices |
| `repeated-reflection-in-loop` | [Repeated Reflection in Loop](repeated-reflection-in-loop.md) | INFO | O(n·reflection) where O(n) suffices |
| `hidden-nested-loop` | [Hidden Nested Loop](hidden-nested-loop.md) | WARNING | O(n·m) hidden behind method call |
| `expensive-callback` | [Expensive Callback](expensive-callback.md) | WARNING | Expensive operations inside HOF callbacks |

## Sort abuse

Wasteful sort operations or expensive work inside sort comparators.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `sort-for-last` | [Sort for Last](sort-for-last.md) | WARNING | O(n log n) where O(n) suffices |
| `expensive-sort-comparator` | [Expensive Sort Comparator](expensive-sort-comparator.md) | WARNING | O(n² log n) where O(n log n) suffices |
| `filter-after-sort` | [Filter After Sort](filter-after-sort.md) | INFO | O(n log n) wasted on filtered-out elements |

## Query patterns

Database and service call patterns that cause excessive I/O.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `n-plus-one-query` | [N+1 Query](n-plus-one-query.md) | WARNING | O(n·IO) where O(1·IO) suffices |
| `bulk-load-for-single-lookup` | [Bulk Load for Single Lookup](bulk-load-for-single-lookup.md) | WARNING | O(n) where O(1) suffices |

## Construction cost

Unnecessary object creation overhead.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `expensive-construction` | [Expensive Construction](expensive-construction.md) | INFO | Unnecessary allocation overhead |

## Redundancy

Repeated computations that could be cached.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `redundant-expensive-call` | [Redundant Expensive Call](redundant-expensive-call.md) | INFO | k·O(f) where O(f) suffices |
| `uncached-getter` | [Uncached Getter](uncached-getter.md) | INFO | k·O(lookup) where O(lookup) suffices |
| `chained-getters` | [Chained Getters](chained-getters.md) | INFO | O(n^k) where O(n) suffices |

## Concurrency

Patterns that undermine parallel execution performance.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `parallel-pipeline-bottleneck` | [Parallel Pipeline Bottleneck](parallel-pipeline-bottleneck.md) | WARNING | Synchronization bottleneck in parallel pipeline |

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
