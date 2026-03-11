---
tags:
  - Kotlin
---

# Kotlin

**Parser:** Text-based · **File extensions:** `.kt`, `.kts` · **Rules:** 23/23

Kotlin uses a lightweight text-based parser that recognizes Kotlin-specific syntax and stdlib patterns.

## What the parser handles

- `val`/`var` declarations with type inference
- Extension functions and properties
- Lambda expressions and trailing lambdas
- Kotlin collection operations (`filter`, `map`, `flatMap`, `forEach`, `groupBy`)
- Scope functions (`let`, `run`, `apply`, `also`, `with`)
- Data classes and companion objects
- String templates

## Kotlin-specific patterns

Algorilla recognizes Kotlin idioms that can hide complexity:

```kotlin
// Flagged: nested lookup via Kotlin stdlib
orders.filter { it.id in idList }  // List.contains via 'in' operator

// Not flagged: O(1) lookup
orders.filter { it.id in idSet }   // Set.contains via 'in' operator
```

Kotlin's concise syntax can make expensive operations look deceptively simple:

```kotlin
// Flagged: sort-for-last
val oldest = people.sortedBy { it.age }.last()

// Better:
val oldest = people.maxBy { it.age }
```

## Scope functions

The parser understands that scope functions like `let`, `run`, and `apply` create execution contexts but don't introduce iteration, so they don't trigger false positives on loop-based rules.

## Known limitations

- Type inference for chained expressions is limited compared to the Java ANTLR parser
- Some complex generic type signatures may not resolve, falling back to unknown type (use type hints as a workaround)
