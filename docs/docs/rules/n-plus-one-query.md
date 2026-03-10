# N+1 Query

**Rule ID:** `n-plus-one-query` · **Severity:** WARNING · **Complexity:** O(n·IO) → O(1·IO + n)

## Description

Detects single-record fetch calls (`findById()`, `getById()`, `countBy*`) inside loops. Each iteration triggers a separate database or service call, turning what could be a single bulk fetch into N sequential round-trips.

## Bad Example

=== "Java"

    ```java
    for (Long orderId : orderIds) {
        Order order = orderRepository.findById(orderId); // DB call per iteration
        results.add(order);
    }
    ```

=== "Groovy"

    ```groovy
    orderIds.each { id ->
        def order = orderRepository.findById(id) // DB call per iteration
        results.add(order)
    }
    ```

## Good Example

```java
Map<Long, Order> orderMap = orderRepository.findAllById(orderIds)
    .stream()
    .collect(Collectors.toMap(Order::getId, Function.identity()));

for (Long orderId : orderIds) {
    Order order = orderMap.get(orderId); // O(1) map lookup
    results.add(order);
}
```

## How Detection Works

1. Identifies loop constructs (for, while, forEach, each, stream operations)
2. Inside each loop, looks for calls matching repository fetch patterns:
    - Method names: `findById`, `getById`, `findOne`, `getOne`, `loadById`, `countBy*`
    - Compound patterns: `getOrderByOrderId`, `findPaymentByTransactionRef`
3. Also checks target variable names for repository indicators (`repository`, `dao`, `store`, `service`, `client`)
4. Supports cross-method resolution to catch hidden repository calls in helper methods

!!! warning "The silent scaling killer"
    N+1 queries are one of the most common performance issues in web applications. They're invisible in development (where the database is local and the dataset is small) and devastating in production (where each query adds network latency).

## Suggestion

> Bulk fetch all needed records before the loop, or build an in-memory Map

## Related Rules

- [Bulk Load for Single Lookup](full-scan-for-single-lookup.md) — loading all records when only one is needed
