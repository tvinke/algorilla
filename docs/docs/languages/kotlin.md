---
tags:
  - Kotlin
  - performance
description: "Kotlin complexity analysis with Algorilla — detects hidden O(n²) in scope functions, sequential coroutine awaits, collection chain traps, and more. 28 rules apply."
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
  ↗ https://tvinke.github.io/algorilla/rules/nested-lookup

      3 │ orders.filter { it.id in activeIds }

  ⎿  filter() over orders   O(orders)
    ⎿  contains on 'activeIds'   O(activeIds) ← bottleneck
```

With a handful of active orders this doesn't matter. At scale it does — and now you know it's there.

## Top complexity traps in Kotlin

### Sequential coroutine awaits

`async { ... }.await()` inside a loop defeats the purpose of coroutines. Each iteration waits for the previous one to complete — you're writing sequential code with extra steps.

```kotlin
for (order in orders) {
    val details = async { httpClient.get("/api/details/${order.id}").body<OrderDetails>() }
        .await() // blocks until this one finishes before starting the next
    results.add(details)
}
```

```
warning  · sequential-async-join-in-loop · O(orders·wait) → O(max-wait)

  await() called inside for-each loop — sequential instead of parallel
  ↗ https://tvinke.github.io/algorilla/rules/sequential-async-join-in-loop
```

Fix: launch all coroutines first, await them together with [`coroutineScope`](https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency) and [`awaitAll`](https://kotlinlang.org/docs/composing-suspending-functions.html#concurrent-using-async):

```kotlin
val results = coroutineScope {
    orders.map { order ->
        async { httpClient.get("/api/details/${order.id}").body<OrderDetails>() }
    }.awaitAll()
}
```

See [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md).

### The scope function that hides a lookup

Kotlin's `let`, `run`, and `also` make code flow nicely but can obscure what's happening inside. A `let` block inside `filter` that does a linear search looks clean and reads well — the performance cost is invisible.

```kotlin
val allowedCodes = getActiveCodes() // List<String>

products.filter { product ->
    product.categoryCode.let { code ->
        allowedCodes.contains(code) // O(n) per product
    }
}
```

```
warning  · nested-lookup · O(products × allowedCodes) → O(products + allowedCodes)

  → Build a HashSet/Map from 'allowedCodes' before the loop
  ↗ https://tvinke.github.io/algorilla/rules/nested-lookup
```

Fix: `val allowedSet = allowedCodes.toHashSet()`. See [nested-lookup](../rules/nested-lookup.md).

### Ktor client calls in a loop

Same pattern as Spring's RestTemplate, but with Ktor's suspend functions. Each `httpClient.get()` is a full HTTP round-trip.

```kotlin
for (user in users) {
    val profile = httpClient.get("/api/profiles/${user.id}")
        .body<UserProfile>()
    profiles.add(profile)
}
```

```
warning  · io-in-loop · O(users·IO) → O(1·IO + users)

  get() called inside for-each loop — IO per iteration
  → Batch the IO operation outside the loop, or use a bulk API
  ↗ https://tvinke.github.io/algorilla/rules/io-in-loop
```

Fix: use `async` to parallelize, or call a batch endpoint. See [io-in-loop](../rules/io-in-loop.md).

### Collection chain with hidden sort

Kotlin's collection stdlib makes chaining look effortless. But `sortedBy` followed by `first()` is O(n log n) when `minBy` does it in O(n).

```kotlin
val cheapest = products.sortedBy { it.price }.first()
```

```
warning  · sort-for-last · Sort abuse · O(n log n) → O(n)

  → Use minByOrNull { it.price } — avoids sorting the entire collection
  ↗ https://tvinke.github.io/algorilla/rules/sort-for-last
```

One character difference in readability, orders of magnitude difference at scale. See [sort-for-last](../rules/sort-for-last.md).

## What Kotlin does well (and why it's tricky to scan)

Kotlin's collection stdlib makes everything look like a single expression. Chaining `filter`, `map`, `sortedBy`, `groupBy` reads beautifully — but each operation has a cost, and the chain hides the multiplication.

Algorilla understands Kotlin's collection operations and treats them as iteration boundaries, the same way it treats `for` loops in Java.

## Scope functions

The parser knows that `let`, `run`, `apply`, `also`, and `with` create execution contexts but don't introduce iteration. They won't trigger false positives on loop-based rules.

## Framework knowledge

| Framework | What Algorilla knows | Most relevant rules |
|-----------|---------------------|-------------------|
| **Kotlin Coroutines** | `launch`, `async`, `runBlocking`, `Flow`, `Channel`, `Dispatchers`, `Mutex` | [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md), [io-in-loop](../rules/io-in-loop.md) |
| **Ktor** | Routing, client (`get`, `post`), content negotiation, WebSockets | [io-in-loop](../rules/io-in-loop.md), [expensive-construction](../rules/expensive-construction.md) |

Kotlin also inherits all Java framework knowledge — Spring, JPA, Guava, Reactor, and Apache Commons all apply.

## Known limitations

- Type inference for chained expressions is limited compared to Java — the `in` operator example above works because `activeIds` is declared as a `List`, but complex generic chains may fall back to unknown type
- Use [type hints](../getting-started/configuration.md) in `.algorilla.yml` as a workaround when types can't be resolved

## See also

- [All rules](../rules/index.md) — the full set of patterns Algorilla detects, all 28 apply to Kotlin
- [Framework support](frameworks.md) — built-in knowledge of Kotlin Coroutines and Ktor
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
