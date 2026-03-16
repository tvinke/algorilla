---
tags:
  - Java
  - Kotlin
  - Groovy
  - performance
  - strings
description: "Detects String.concat() calls inside loops — each call copies the entire accumulated string, turning O(n) iteration into O(n²) character copies."
---

# String Concat in Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `string-concat-in-loop` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | HIGH — structurally proven |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n²) → O(n) |

## The problem

`String.concat()` inside a loop. Strings in Java (and Kotlin, and Groovy) are immutable — every `concat()` creates a brand new `String` and copies the _entire_ accumulated content into it. On iteration 1, it copies 1 word. On iteration 100, it copies 100 words. On iteration 1,000, it copies 1,000 words. The total copies are 1 + 2 + 3 + ... + n = n(n+1)/2 — that's O(n²).

For 1,000 items averaging 20 characters each, that's roughly 10 million character copies instead of 20,000. `StringBuilder` appends in-place (amortized O(1) per append) and avoids the entire problem.

This is one of the oldest known Java performance pitfalls — it dates back to the JDK 1.0 era. Modern JVMs optimize `+` concatenation in _some_ cases (JEP 280, `invokedynamic`-based string concat), but `String.concat()` calls still create a new object every time.

## Where this shows up

- In report generators that build CSV or text output line by line
- In logging code that assembles multi-part messages inside loops
- In serialization utilities that convert collections to delimited strings
- In template engines or code generators that accumulate output

## The pattern

=== "Java"

    ```java
    String report = "";
    for (Order order : orders) {
        report = report.concat(order.getName()).concat(", "); // (1)!
    }
    ```

    1. Copies the entire accumulated string on every iteration

=== "Kotlin"

    ```kotlin
    var report = ""
    for (order in orders) {
        report = report + order.name + ", " // same problem — string is immutable
    }
    ```

=== "Groovy"

    ```groovy
    def report = ""
    orders.each { order ->
        report = report.concat(order.name).concat(", ")
    }
    ```

## The fix

=== "Java (StringBuilder)"

    ```java
    StringBuilder report = new StringBuilder();
    for (Order order : orders) {
        report.append(order.getName()).append(", "); // O(1) amortized per append
    }
    return report.toString();
    ```

=== "Java (Collectors.joining)"

    [`Collectors.joining`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Collectors.html#joining(java.lang.CharSequence)) handles the separator cleanly:

    ```java
    String report = orders.stream()
        .map(Order::getName)
        .collect(Collectors.joining(", "));
    ```

=== "Kotlin (buildString)"

    [`buildString`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/build-string.html) wraps a `StringBuilder` in an idiomatic Kotlin block:

    ```kotlin
    val report = buildString {
        for (order in orders) {
            append(order.name).append(", ")
        }
    }
    ```

=== "Kotlin (joinToString)"

    Or use [`joinToString`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/join-to-string.html) for a one-liner:

    ```kotlin
    val report = orders.joinToString(", ") { it.name }
    ```

=== "Groovy"

    ```groovy
    def report = orders.collect { it.name }.join(", ")
    ```

## Suggestion variants

=== "UseAlternativeAPI"
    > Use StringBuilder.append() — avoids copying the entire string on each iteration

    Points to the standard fix: replace immutable `concat()` with a mutable `StringBuilder`.

## When to ignore this

If the loop is bounded to a small constant (building a string from 3-5 items), the overhead is negligible and `concat()` reads more clearly than `StringBuilder`. Micro-benchmarks on modern JVMs show the crossover point around 10-20 iterations — below that, the `StringBuilder` overhead (object allocation, capacity management) can equal the copying cost.

## Note

The more common `+=` operator on strings (`result += item`) has the same O(n²) behavior but is not yet detected by this rule (requires parser-level support for assignment expressions). The `String.concat()` variant is detected. `Collectors.joining()` and `joinToString()` are always safe.

## Related rules

**Loop overhead cluster** — patterns where per-iteration cost is higher than it looks:

- [In-Loop Collection Building](in-loop-collection-building.md) — similar pattern with collections instead of strings (building a list by copying on every iteration)
- [Quadratic Removal](quadratic-removal.md) — another O(n²) pattern from repeated shifting in array-backed lists

**Redundancy cluster:**

- [Loop-Invariant Hoisting](loop-invariant-hoisting.md) — when the string being concatenated is the same every iteration (constant part could be hoisted)

**Language-specific examples:**

- [Java](../languages/java.md#string-concatenation-the-hard-way) — `String.concat()` in a loop with StringBuilder fix

## Configuration

Disable globally:

```yaml
rules:
  string-concat-in-loop:
    enabled: false
```

Suppress inline:

```java
// algorilla:ignore string-concat-in-loop
for (String part : parts) {
    result = result.concat(part);
}
```
