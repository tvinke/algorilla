# The hidden N+1

Some performance problems aren't about CPU at all — they're about I/O. The N+1 query pattern turns a single bulk database call into N individual round-trips, each adding network latency. It's invisible on your local machine, where the database is on localhost and the dataset has 10 rows. It surfaces in production, where every query crosses a network and the dataset has thousands of rows.

It's a common pattern in web applications and easy to miss in code review.

## One query is fine

Fetching a single record by ID is a straightforward database call:

```java
Order order = orderRepository.findById(orderId);
```

This executes one SQL query, returns one result. Constant-time relative to the table size (assuming an indexed primary key). Nothing to worry about.

## Put it in a loop

Now you need to load orders for a list of IDs:

```java
List<Order> results = new ArrayList<>();
for (Long orderId : orderIds) {
    Order order = orderRepository.findById(orderId);
    results.add(order);
}
```

This looks simple enough. For each ID, fetch the order, add it to the list. A reviewer sees a clean loop with a straightforward repository call.

But each `findById()` is a separate database round-trip. With 10 order IDs, that's 10 queries. With 2,000 order IDs, that's 2,000 queries. The "1" is the initial query that produced the list of IDs; the "N" is the N follow-up queries — hence "N+1."

## Local dev hides the cost

This is what makes N+1 so insidious. On your development machine:

| Factor | Local dev | Production |
|--------|-----------|------------|
| Database location | localhost | Network hop (1-5ms per query) |
| Dataset size | 10-50 rows | 10,000+ rows |
| Connection pooling | Idle connections ready | Pool contention under load |
| Total for 10 queries | ~5ms | ~50ms |
| Total for 2,000 queries | ~100ms | ~10 seconds |

Your test suite runs with 5 order IDs on an in-memory H2 database. The loop completes in microseconds. The test passes. The code ships.

In production, the same loop hits a PostgreSQL instance over a network, 2,000 times. The endpoint that used to respond in 50ms now takes 10 seconds. The downstream service times out. The error rate spikes. And the stack trace points at... nothing unusual. Just a loop calling `findById()`.

!!! danger "Stack traces don't help"

    N+1 doesn't throw exceptions or produce errors. It just makes everything slow. You won't see it in error logs — you'll see it in latency metrics, database connection pool exhaustion, and cascading timeouts.

## It hides in helper methods

Like the [nested-lookup pattern](hidden-complexity.md#step-2-the-loop-hides-across-methods), N+1 often hides behind method indirection:

```java
// OrderService.java
public List<OrderDto> enrichOrders(List<Long> orderIds) {
    return orderIds.stream()
        .map(this::loadAndEnrich)  // what does this cost?
        .collect(Collectors.toList());
}
```

```java
// OrderService.java (same file, 50 lines down)
private OrderDto loadAndEnrich(Long orderId) {
    Order order = orderRepository.findById(orderId);  // DB call — hidden here
    Payment payment = paymentRepository.findByOrderId(order.getId());  // Another DB call
    return toDto(order, payment);
}
```

Now there are 2N database calls — one per order, one per payment. The stream pipeline in `enrichOrders()` looks like a simple mapping operation. The reviewer sees `.map(this::loadAndEnrich)` and moves on.

## What algorilla does about this

Algorilla detects single-record fetch calls inside loops, including stream operations like `.map()` and `.forEach()`. It follows method calls to catch patterns hidden behind helper methods.

Running algorilla on the simple loop example:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · n-plus-one-query · Query patterns · O(orderIds × IO) → O(1 × IO + orderIds)
    com.example.shop.service.OrderService:24

      Repository call 'findById' inside for-each loop — N+1 query
      → Bulk fetch all needed records before the loop, or build an in-memory Map
      ↳ https://tvinke.github.io/algorilla/rules/n-plus-one-query

          22 │ List<Order> results = new ArrayList<>();
          23 │ for (Long orderId : orderIds) {
          24 │     Order order = orderRepository.findById(orderId);
          25 │     results.add(order);

      ⎿  for-each loop over orderIds OrderService.java:23 O(orderIds)
        ⎿  findById on 'orderRepository' OrderService.java:24 O(IO) ← bottleneck
```

## The fix

Fetch all records in a single query, then look them up by key:

```java
// Before — one DB call per orderId: N round-trips
List<Order> results = new ArrayList<>();
for (Long orderId : orderIds) {
    Order order = orderRepository.findById(orderId);
    results.add(order);
}

// After — one bulk DB call + O(1) map lookups: 1 round-trip
Map<Long, Order> orderMap = orderRepository.findAllById(orderIds)
    .stream()
    .collect(Collectors.toMap(Order::getId, Function.identity()));

List<Order> results = new ArrayList<>();
for (Long orderId : orderIds) {
    results.add(orderMap.get(orderId));
}
```

The bulk `findAllById()` executes a single `SELECT ... WHERE id IN (...)` query. The map gives O(1) lookups. Total database round-trips: 1 instead of N.

For the helper method variant, the refactoring is bigger — you need to restructure the code to bulk-fetch first, then enrich:

```java
public List<OrderDto> enrichOrders(List<Long> orderIds) {
    // Bulk fetch everything up front: 2 queries total
    Map<Long, Order> orderMap = orderRepository.findAllById(orderIds)
        .stream().collect(Collectors.toMap(Order::getId, Function.identity()));
    Map<Long, Payment> paymentMap = paymentRepository.findAllByOrderIds(orderIds)
        .stream().collect(Collectors.toMap(Payment::getOrderId, Function.identity()));

    // Assemble from maps: O(1) per order
    return orderIds.stream()
        .map(id -> toDto(orderMap.get(id), paymentMap.get(id)))
        .collect(Collectors.toList());
}
```

This changes the method's shape significantly — you go from "process one at a time" to "fetch everything, then assemble." In a layered architecture with service boundaries, this refactoring can ripple across multiple classes.

!!! tip "Frameworks can help"

    Spring Data's `@EntityGraph`, JPA's `JOIN FETCH`, and Hibernate's batch fetching can address N+1 at the ORM level. But they require understanding *where* the N+1 is happening — which is exactly what algorilla tells you.

For more on the Big-O notation used here, see the [Big-O primer](concepts/big-o-primer.md). For the full list of patterns algorilla detects, see the [rules overview](rules/index.md).
