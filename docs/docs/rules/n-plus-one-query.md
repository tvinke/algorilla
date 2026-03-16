---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
  - Spring Data
  - JPA
  - performance
  - database
description: "Detects single-record database or service fetch calls inside loops — the classic N+1 query problem. Shows bulk-fetch alternatives for Spring Data, JPA, GORM, and fetch()."
---

# N+1 Query

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `n-plus-one-query` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Query patterns |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n·IO) → O(1·IO + n) |

## The problem

A single-record fetch — `findById()`, `getById()`, a REST call per ID — inside a loop. The first query loads a list of IDs (that's the "1"), then N subsequent queries load each related record one at a time (that's the "+N"). Every iteration pays full network round-trip cost: TCP handshake, query parsing, result serialization, response transfer.

With a local database and 10 records, this takes milliseconds. With a remote database and 10,000 records, it takes minutes. The math is brutal: 10,000 × 5ms network latency = 50 seconds of pure waiting, where a single `IN (...)` query would take 20ms.

N+1 queries are a common reason web applications slow down after launch. They're hard to notice in dev (local DB, small dataset) but add up quickly in production (remote DB, real data volumes).

## Where this shows up

- In Spring Data services that loop over parent IDs and call `findById()` for each child
- In REST API aggregation layers that call another microservice once per item
- In Grails controllers using GORM dynamic finders inside `.each {}`
- In GraphQL resolvers that fetch related entities one by one per parent
- In batch jobs that process records sequentially instead of in chunks

## The pattern

=== "Java (Spring Data)"

    ```java
    for (Long orderId : orderIds) {
        Order order = orderRepository.findById(orderId) // (1)!
            .orElseThrow();
        results.add(process(order));
    }
    ```

    1. DB round-trip per iteration — 1,000 IDs = 1,000 queries

=== "Java (JPA)"

    ```java
    for (Long orderId : orderIds) {
        Order order = entityManager.find(Order.class, orderId); // DB call per iteration
        results.add(process(order));
    }
    ```

=== "Groovy (GORM)"

    ```groovy
    orderIds.each { id ->
        def order = Order.findById(id) // DB call per iteration
        results.add(process(order))
    }
    ```

=== "JavaScript (fetch)"

    ```javascript
    for (const id of orderIds) {
        const res = await fetch(`/api/orders/${id}`); // HTTP call per iteration
        results.push(await res.json());
    }
    ```

## The fix

### Bulk fetch with Spring Data

=== "findAllById"

    ```java
    List<Order> orders = orderRepository.findAllById(orderIds); // single query
    for (Order order : orders) {
        results.add(process(order));
    }
    ```

=== "Map for random access"

    ```java
    Map<Long, Order> orderMap = orderRepository.findAllById(orderIds)
        .stream()
        .collect(Collectors.toMap(Order::getId, Function.identity())); // O(n) build

    for (Long orderId : orderIds) {
        Order order = orderMap.get(orderId); // O(1) lookup, preserves original order
        if (order != null) results.add(process(order));
    }
    ```

=== "Custom @Query with IN"

    ```java
    @Query("SELECT o FROM Order o WHERE o.id IN :ids")
    List<Order> findByIds(@Param("ids") Collection<Long> ids);
    ```

### Bulk fetch with [JPA](https://jakarta.ee/specifications/persistence/3.1/apidocs/jakarta.persistence/jakarta/persistence/typedquery)

```java
TypedQuery<Order> query = entityManager.createQuery(
    "SELECT o FROM Order o WHERE o.id IN :ids", Order.class);
query.setParameter("ids", orderIds);
List<Order> orders = query.getResultList(); // single query
```

### Batch fetch in JavaScript

```javascript
const res = await fetch('/api/orders/batch', {
    method: 'POST',
    body: JSON.stringify({ ids: orderIds }),
});
const orders = await res.json(); // single request
```

Or with [`Promise.all`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all) when a batch endpoint doesn't exist:

```javascript
const orders = await Promise.all(
    orderIds.map(id => fetch(`/api/orders/${id}`).then(r => r.json()))
); // parallel, not sequential
```

## Suggestion variants

=== "Freeform"
    > Bulk fetch all needed records before the loop, or build an in-memory Map

    The suggestion text varies based on the specific call pattern detected. For repository calls, it might mention `findAllById`; for generic service calls, it gives broader advice.

## When to ignore this

If the loop processes records one at a time _on purpose_ — for example, because each record requires its own transaction boundary, or because loading all records at once would exceed memory — suppressing is legitimate. Same if the "loop" actually processes at most 2-3 items by design.

```java
// algorilla:ignore n-plus-one-query — each order needs its own transaction
for (Long orderId : orderIds) {
    transactionTemplate.execute(status -> {
        Order order = orderRepository.findById(orderId).orElseThrow();
        // ...
    });
}
```

## How detection works

1. Identifies loop constructs (`for`, `while`, `forEach`, `each`, stream operations)
2. Inside each loop, looks for calls matching repository fetch patterns:
    - Method names: `findById`, `getById`, `findOne`, `getOne`, `loadById`, `countBy*`
    - Compound patterns: `getOrderByOrderId`, `findPaymentByTransactionRef`
3. Checks target variable names for repository indicators (`repository`, `dao`, `store`, `service`, `client`)
4. Cross-method resolution catches hidden repository calls in helper methods

!!! warning "Worth investigating"
    N+1 queries tend to be high-confidence findings — the pattern is structurally clear. If Algorilla flags one, it's usually worth a look.

## Related rules

**I/O cluster** — different patterns, same root cause (too many I/O round-trips):

- [IO In Loop](io-in-loop.md) — HTTP, file, and generic network calls in loops
- [Lazy Loading In Loop](lazy-loading-in-loop.md) — ORM lazy-loaded associations triggered per iteration
- [Bulk Load for Single Lookup](bulk-load-for-single-lookup.md) — the inverse problem: loading _all_ records when only one is needed

**Language-specific examples:**

- [Java](../languages/java.md#n1-queries-with-spring-data-jpa) — Spring Data JPA `findById` in a loop
- [Groovy](../languages/groovy.md#gorm-dynamic-finders-in-each-) — GORM dynamic finders in `.each {}`

## Configuration

Disable globally:

```yaml
rules:
  n-plus-one-query:
    enabled: false
```

Suppress inline:

```java
// algorilla:ignore n-plus-one-query
for (Long id : ids) {
    Order order = orderRepository.findById(id).orElseThrow();
}
```
