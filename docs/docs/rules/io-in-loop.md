---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
description: "Detects HTTP, database, and file system calls inside loops. Each iteration pays full round-trip latency, turning O(n) logic into O(n·IO) wall-clock time."
---

# IO In Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `io-in-loop` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | HIGH (unambiguous IO) · MEDIUM (candidate match) |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n·IO) → O(1·IO + n) |

## The problem

A network, database, or file system call inside a loop body. Each iteration incurs a full I/O round-trip — network latency, disk seek, query execution — so the total wall-clock time scales linearly with the loop size. The computational cost of the loop might be trivial, but the wall-clock cost is anything but.

A loop that makes 100 HTTP calls at 50ms each takes 5 seconds _minimum_, regardless of how fast the server responds. A single batch call takes 50ms + a bit of extra payload processing. The gap only widens with more iterations.

Unlike CPU-bound performance problems, this one doesn't show up in profiler flame graphs the way you'd expect. The thread is _waiting_, not working. You see it in latency metrics, not CPU utilization.

## Where this shows up

- In Spring services that call `RestTemplate.getForObject()` per item in a list
- In data migration scripts that write files or make API calls one record at a time
- In JavaScript backends that `await fetch()` sequentially inside a `for` loop
- In Kotlin coroutines that call `httpClient.get()` inside a `forEach` without parallelism
- In JDBC code that prepares and executes a statement per ID instead of batching

## The pattern

=== "Java (RestTemplate)"

    ```java
    for (Order order : orders) {
        OrderDetails d = restTemplate.getForObject( // (1)!
            "/api/details/" + order.getId(), OrderDetails.class);
        details.add(d);
    }
    ```

    1. HTTP round-trip per iteration — 500 orders = 500 HTTP calls

=== "Java (JDBC)"

    ```java
    for (String id : orderIds) {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM orders WHERE id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery(); // DB round-trip per iteration
        result.add(mapRow(rs));
    }
    ```

=== "Kotlin (Ktor)"

    ```kotlin
    for (order in orders) {
        val details = httpClient.get("/api/details/${order.id}") // HTTP per iteration
            .body<OrderDetails>()
        results.add(details)
    }
    ```

=== "JavaScript"

    ```javascript
    for (const id of orderIds) {
        const response = await fetch(`/api/orders/${id}`); // HTTP per iteration
        orderDetails.push(await response.json());
    }
    ```

## The fix

=== "Java (batch call)"

    ```java
    // Single batch request
    List<OrderDetails> details = restTemplate.postForObject(
        "/api/details/batch", orderIds, List.class);
    ```

=== "Java (JDBC IN clause)"

    ```java
    String placeholders = String.join(",", Collections.nCopies(orderIds.size(), "?"));
    PreparedStatement ps = conn.prepareStatement(
        "SELECT * FROM orders WHERE id IN (" + placeholders + ")");
    for (int i = 0; i < orderIds.size(); i++) {
        ps.setString(i + 1, orderIds.get(i));
    }
    ResultSet rs = ps.executeQuery(); // single query
    ```

=== "Kotlin (async parallel)"

    Use [`coroutineScope`](https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency) with [`async` and `awaitAll`](https://kotlinlang.org/docs/composing-suspending-functions.html#concurrent-using-async) to run all calls concurrently:

    ```kotlin
    val results = coroutineScope {
        orders.map { order ->
            async { httpClient.get("/api/details/${order.id}").body<OrderDetails>() }
        }.awaitAll() // parallel, not sequential
    }
    ```

=== "JavaScript (Promise.all)"

    Use [`Promise.all`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all) to fire all requests concurrently:

    ```javascript
    const orderDetails = await Promise.all(
        orderIds.map(id =>
            fetch(`/api/orders/${id}`).then(r => r.json())
        )
    ); // parallel requests — total time ≈ max(individual times)
    ```

=== "JavaScript (batch endpoint)"

    ```javascript
    const response = await fetch('/api/orders/batch', {
        method: 'POST',
        body: JSON.stringify(orderIds),
    });
    const orderDetails = await response.json(); // single request
    ```

## Suggestion variants

=== "Freeform"
    > Batch the IO operation outside the loop, or use a bulk API

    The suggestion text adapts to the detected I/O pattern — HTTP calls get batch endpoint advice, database calls get bulk query guidance, file operations get batching suggestions.

## When to ignore this

Some I/O _has_ to happen per iteration. Rate-limited APIs that only accept one item at a time, file-per-record output requirements, or explicit per-item transaction boundaries are all valid reasons to suppress. Also fine when the loop is bounded to a small constant (processing 3 config files, not 3,000 records).

```java
// algorilla:ignore io-in-loop — rate-limited API, one call per item required
for (Order order : orders) {
    rateLimiter.acquire();
    externalApi.submit(order);
}
```

## Covered IO categories

The rule detects I/O methods from YAML semantics files. Coverage includes:

- **Database:** JDBC (`executeQuery`, `prepareStatement`), Groovy SQL (`eachRow`, `rows`)
- **HTTP:** Spring RestTemplate (`getForObject`, `exchange`, etc.), Ktor Client, `fetch()` in JS
- **File system:** Kotlin file extensions (`readText`, `writeText`), Node.js sync fs operations

Framework overlays (Spring, Ktor, Node) extend the base language methods with framework-specific I/O calls.

## Related rules

**I/O cluster** — each rule catches a specific flavor of "too many I/O calls":

- [N+1 Query](n-plus-one-query.md) — specifically targets repository fetch patterns like `findById` in a loop
- [Lazy Loading In Loop](lazy-loading-in-loop.md) — ORM lazy-loaded associations triggered per iteration
- [Bulk Load for Single Lookup](bulk-load-for-single-lookup.md) — loading all records when only one is needed

**Loop overhead cluster** — when the cost inside the loop is CPU rather than I/O:

- [Nested Lookup](nested-lookup.md) — linear search in a loop (CPU-bound O(n²))
- [String Concat in Loop](string-concat-in-loop.md) — string copying in a loop (memory-bound O(n²))

**Language-specific examples:**

- [JavaScript](../languages/javascript.md#await-in-a-for-loop) — sequential `await fetch()` in a loop
- [Kotlin](../languages/kotlin.md#ktor-client-calls-in-a-loop) — Ktor HTTP client calls per iteration

## Configuration

Disable globally:

```yaml
rules:
  io-in-loop:
    enabled: false
```

Suppress inline:

```java
// algorilla:ignore io-in-loop
for (Order order : orders) {
    externalService.notify(order);
}
```
