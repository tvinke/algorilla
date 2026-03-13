---
tags:
  - Kotlin
---

# Kotlin

!!! warning "Alpha"
    Kotlin support is early-stage. Expect higher false-positive rates, especially on non-standard codebases. Feedback welcome — file issues for patterns that don't look right.

Kotlin's concise syntax is a feature — until it hides the cost of what's happening underneath:

```kotlin
val activeIds = repository.getActiveOrderIds()

orders.filter { it.id in activeIds }
    .forEach { process(it) }
```

The `in` keyword is syntactic sugar for `contains()`. On a `List`, that's O(n) — called once per order. Algorilla catches it:

```
warning  · nested-lookup · Loop amplifiers · O(orders × activeIds) → O(orders + activeIds)

  Linear contains on 'activeIds' inside filter()
  → Build a HashSet/Map from 'activeIds' before the loop

      3 │ orders.filter { it.id in activeIds }

  ⎿  filter() over orders   O(orders)
    ⎿  contains on 'activeIds'   O(activeIds) ← bottleneck
```

With a handful of active orders this doesn't matter. At scale it does — and now you know it's there.

## What Kotlin does well (and why it's tricky to scan)

Kotlin's collection stdlib makes everything look like a single expression. Chaining `filter`, `map`, `sortedBy`, `groupBy` reads beautifully — but each operation has a cost, and the chain hides the multiplication.

```kotlin
// Flagged: sort-for-last
val oldest = people.sortedBy { it.age }.last()

// Better:
val oldest = people.maxBy { it.age }
```

Algorilla understands Kotlin's collection operations and treats them as iteration boundaries, the same way it treats `for` loops in Java.

## Scope functions

The parser knows that `let`, `run`, `apply`, `also`, and `with` create execution contexts but don't introduce iteration. They won't trigger false positives on loop-based rules.

## Known limitations

- Type inference for chained expressions is limited compared to Java — the `in` operator example above works because `activeIds` is declared as a `List`, but complex generic chains may fall back to unknown type
- Use [type hints](../getting-started/configuration.md) in `.algorilla.yml` as a workaround when types can't be resolved

## See also

- [All rules](../rules/index.md) — the full set of patterns Algorilla detects, all 23 apply to Kotlin
- [Framework support](frameworks.md) — built-in knowledge of Kotlin Coroutines and Ktor for fewer false positives
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
