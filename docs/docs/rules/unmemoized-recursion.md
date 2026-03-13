# Unmemoized Recursion

**ID:** `unmemoized-recursion` · **Category:** Redundancy · **Severity:** WARNING (HIGH confidence) or INFO (LOW confidence)

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
