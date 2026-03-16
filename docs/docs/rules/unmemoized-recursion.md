# Unmemoized Recursion

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `unmemoized-recursion` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **Confidence** | HIGH (2+ calls) · MEDIUM (loop) · LOW (single call) |
    | **Category** | Redundancy |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(2^n) → O(n) |

Flags recursive functions that recompute the same subproblem multiple times because there's no caching or memoization.

## What it detects

A function that calls itself recursively, where the same inputs may be evaluated more than once. The classic example is naive Fibonacci:

```java
int fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);  // exponential: fib(3) computed many times
}
```

With two recursive calls using overlapping arguments (`n-1` and `n-2`), this is O(2^n) instead of O(n) with memoization.

## Confidence tiers

| Pattern | Confidence | Severity |
|---------|-----------|----------|
| 2+ recursive calls with different arguments | HIGH | WARNING |
| Recursive call inside a loop body | MEDIUM | WARNING |
| Single recursive call (non-tree-traversal) | LOW | INFO |

## What it skips

- Functions with memoization patterns (`computeIfAbsent`, `getOrPut`, cache map lookups)
- Functions with visited-set tracking (`visited.add()`, `seen.contains()`)
- Tree traversals where the recursive call uses a child accessor (`node.left`, `node.children`, `getRight()`)

## Fix

Add a cache keyed on the function's arguments:

```java
Map<Integer, Integer> cache = new HashMap<>();
int fib(int n) {
    if (n <= 1) return n;
    return cache.computeIfAbsent(n, k -> fib(k - 1) + fib(k - 2));
}
```
