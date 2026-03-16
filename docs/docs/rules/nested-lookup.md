---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
description: "Detects linear lookup operations (contains, indexOf, includes) inside loops that turn O(n) into O(n²). Shows how to fix with HashSet or Map."
---

# Nested Lookup

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `nested-lookup` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | HIGH (type-confirmed) · MEDIUM (no type info) |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n²) → O(n) |

## The problem

A linear lookup — `contains()`, `indexOf()`, `includes()`, `find()`, `any()` — inside a loop body. Each call walks the entire target collection, so the combined cost is O(outer × target). For two lists of 10,000 items each, that's 100 million comparisons where 10,000 would do.

This pattern survives code review because it _reads_ fine. The loop looks like O(n), and `contains()` looks like a simple boolean check. The quadratic cost only shows up at scale — usually in production, when the dataset grows past whatever worked in testing. By that point nobody remembers this particular loop as the bottleneck.

It's also the single most common finding Algorilla reports across the codebases it scans. Almost every project has at least one.

## Where this shows up

- In Spring services that filter entities against a list of IDs fetched from another source
- In batch processors that skip "already seen" items using a `List` instead of a `Set`
- In data import pipelines that deduplicate by checking `ArrayList.contains()` before adding
- In React components that filter `props.items` against a local exclusion array on every render
- In Groovy Grails controllers that use `.findAll { list.contains(it.id) }`

## The pattern

=== "Java"

    ```java
    for (Order order : orders) {
        if (discountedProductIds.contains(order.getProductId())) { // (1)!
            applyDiscount(order);
        }
    }
    ```

    1. `discountedProductIds` is a `List` — `contains()` is O(n) per call

=== "Kotlin"

    ```kotlin
    orders.filter { order ->
        discountedProductIds.contains(order.productId) // O(n) per iteration
    }.forEach { applyDiscount(it) }
    ```

=== "Groovy"

    ```groovy
    orders.each { order ->
        if (discountedProductIds.contains(order.productId)) { // O(n) per iteration
            applyDiscount(order)
        }
    }
    ```

=== "JavaScript"

    ```javascript
    orders.forEach(order => {
        if (discountedProductIds.includes(order.productId)) { // O(n) per iteration
            applyDiscount(order);
        }
    });
    ```

## The fix

### HashSet approach

Pre-build a `Set` from the lookup target. Set lookups are O(1), turning the loop from O(n²) to O(n).

=== "Java"

    ```java
    Set<String> discountedSet = new HashSet<>(discountedProductIds); // O(n) one-time cost
    for (Order order : orders) {
        if (discountedSet.contains(order.getProductId())) { // O(1) per iteration
            applyDiscount(order);
        }
    }
    ```

=== "Kotlin"

    Use [`toHashSet()`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.collections/to-hash-set.html) to convert the list before the loop:

    ```kotlin
    val discountedSet = discountedProductIds.toHashSet() // O(n) one-time cost
    orders.filter { it.productId in discountedSet } // O(1) per check
        .forEach { applyDiscount(it) }
    ```

=== "Groovy"

    ```groovy
    def discountedSet = discountedProductIds.toSet() // O(n) one-time cost
    orders.findAll { it.productId in discountedSet } // O(1) per check
        .each { applyDiscount(it) }
    ```

=== "JavaScript"

    ```javascript
    const discountedSet = new Set(discountedProductIds); // O(n) one-time cost
    orders.forEach(order => {
        if (discountedSet.has(order.productId)) { // O(1) per iteration
            applyDiscount(order);
        }
    });
    ```

### Map approach

When you need the _value_ associated with each key (not just membership), use a `Map`:

=== "Java"

    ```java
    Map<String, BigDecimal> discountByProductId = discounts.stream()
        .collect(Collectors.toMap(Discount::getProductId, Discount::getAmount));

    for (Order order : orders) {
        BigDecimal discount = discountByProductId.get(order.getProductId()); // O(1)
        if (discount != null) {
            order.applyDiscount(discount);
        }
    }
    ```

=== "JavaScript"

    ```javascript
    const discountByProductId = new Map(
        discounts.map(d => [d.productId, d.amount])
    );

    for (const order of orders) {
        const discount = discountByProductId.get(order.productId); // O(1)
        if (discount !== undefined) {
            order.applyDiscount(discount);
        }
    }
    ```

## Suggestion variants

=== "PreBuildStructure"
    > Build a HashSet from 'discountedProductIds' before the loop

    The default suggestion when the lookup target is a `List`, `ArrayList`, or array.


## When to ignore this

The `HashSet` conversion has overhead: memory for the hash table, the cost of hashing every element. For small collections (under ~50 items), the overhead may exceed the savings. If you _know_ the collection stays small and the loop runs infrequently, suppressing is reasonable:

```java
// algorilla:ignore nested-lookup — productIds always < 20 items here
for (Order order : orders) {
    if (productIds.contains(order.getProductId())) { ... }
}
```

Also safe to ignore when the collection is a `LinkedHashSet` or `TreeSet` that Algorilla can't resolve the type of — those are already O(1) or O(log n).

## How detection works

1. The parser identifies loop constructs (`for`, `while`, `forEach`, `each`, stream operations)
2. Inside each loop body, it looks for linear lookup method calls (`contains`, `indexOf`, `find`, `filter`, `any`, `some`, `includes`)
3. It resolves the target collection's type from variable declarations
4. If the type is not a known O(1) structure (`Set`, `Map`, `HashMap`, etc.), a finding is reported
5. When types can't be resolved, the rule falls back to [name-based heuristics](../concepts/how-detection-works.md#name-based-heuristics-low-confidence) with MEDIUM confidence

## Related rules

**Lookup cluster** — rules that catch different flavors of the same problem:

- [Repeated Linear Scan](repeated-linear-scan.md) — multiple lookups on the same collection outside loops. Same root cause (linear search on a list), different context.

**I/O cluster** — when the "lookup" is actually a network call:

- [N+1 Query](n-plus-one-query.md) — `repository.findById()` in a loop is the database version of this pattern
- [IO In Loop](io-in-loop.md) — HTTP/file calls in loops, the network-latency version

**Language-specific examples** — see this rule in the context of your stack:

- [Java](../languages/java.md#the-hidden-on2-in-arraylistcontains) — `ArrayList.contains()` with type resolution
- [JavaScript](../languages/javascript.md#arrayincludes-inside-filter) — `Array.includes()` inside `filter()`
- [Kotlin](../languages/kotlin.md#the-scope-function-that-hides-a-lookup) — `in` operator and scope functions
- [Groovy](../languages/groovy.md#gdk-collection-methods-and-hidden-iteration) — GDK methods and closures

## Configuration

Disable globally in `.algorilla.yml`:

```yaml
rules:
  nested-lookup:
    enabled: false
```

Or suppress inline for a specific occurrence:

```java
// algorilla:ignore nested-lookup
for (Order order : orders) {
    if (discountedProductIds.contains(order.getProductId())) { ... }
}
```
