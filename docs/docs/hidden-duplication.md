# The hidden triple scan

Most developers know that iterating a collection is O(n). What's less obvious is how quickly those linear scans accumulate when the same collection is streamed multiple times in a single method. Each scan looks clean and independent. The combined cost is invisible during code review because the operations don't *look* related — they're just separate lines doing separate things.

In practice, this is one of the patterns algorilla detects most often.

## One scan is fine

A single stream operation on a collection is O(n). Nothing wrong here:

```java
Order highestTotal = orders.stream()
    .max(Comparator.comparing(Order::getTotal))
    .orElse(null);
```

This visits each order once, keeps track of the max, and returns. Linear time, straightforward.

## Two scans look clean

Now you also need the first cancelled order:

```java
Order highestTotal = orders.stream()
    .max(Comparator.comparing(Order::getTotal))
    .orElse(null);

Order firstCancelled = orders.stream()
    .filter(o -> "CANCELLED".equals(o.getStatus()))
    .findFirst()
    .orElse(null);
```

Still looks fine. Two independent operations, each O(n). Total cost: 2 × O(n). A reviewer sees two clean stream pipelines doing two different things. No alarm bells.

## Three scans start to add up

Then someone adds a count:

```java
Order highestTotal = orders.stream()
    .max(Comparator.comparing(Order::getTotal))
    .orElse(null);

Order firstCancelled = orders.stream()
    .filter(o -> "CANCELLED".equals(o.getStatus()))
    .findFirst()
    .orElse(null);

long paidCount = orders.stream()
    .filter(o -> "PAID".equals(o.getStatus()))
    .count();
```

Three full traversals of the same collection. With 10,000 orders, that's 30,000 element visits. Still fast — but this method is only getting started.

## In practice, it's worse

Real methods don't stop at three. Over time, developers add more stream operations as requirements grow. Each one is a clean, readable one-liner. Nobody refactors the whole method — they just add another pipeline:

```java
public OrderSummary summarize(List<Order> orders) {
    Order highestTotal = orders.stream()
        .max(Comparator.comparing(Order::getTotal)).orElse(null);

    Order firstCancelled = orders.stream()
        .filter(o -> "CANCELLED".equals(o.getStatus())).findFirst().orElse(null);

    long paidCount = orders.stream()
        .filter(o -> "PAID".equals(o.getStatus())).count();

    long shippedCount = orders.stream()
        .filter(o -> "SHIPPED".equals(o.getStatus())).count();

    BigDecimal totalRevenue = orders.stream()
        .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

    Optional<Order> mostRecent = orders.stream()
        .max(Comparator.comparing(Order::getOrderDate));

    List<Order> pendingOrders = orders.stream()
        .filter(o -> "PENDING".equals(o.getStatus())).collect(Collectors.toList());

    // ... build and return summary
}
```

Seven traversals. With 10,000 orders, that's 70,000 element visits. The method reads beautifully — each stream pipeline is self-contained and expressive. A code reviewer would likely approve it without comment.

!!! warning "The readability trap"

    Stream pipelines are designed to be readable in isolation. That's exactly why this pattern slips through — each line is a model of clean, functional-style Java. The problem is only visible when you step back and count how many times `orders.stream()` appears.

## What algorilla does about this

Algorilla detects multiple linear scans on the same collection within a single method. Running it on the three-scan example:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · repeated-linear-scan · Loop amplifiers · O(orders × 3) → O(orders)
    com.example.shop.service.OrderService:42

      3 linear scans on 'orders' in summarize() — combine into a single pass
      → Cache the result of the first lookup, or combine into a single pass
      ↗ https://tvinke.github.io/algorilla/rules/repeated-linear-scan

          38 │ Order highestTotal = orders.stream()
          39 │     .max(Comparator.comparing(Order::getTotal)).orElse(null);
             │
          41 │ Order firstCancelled = orders.stream()
          42 │     .filter(o -> "CANCELLED".equals(o.getStatus())).findFirst().orElse(null);
             │
          44 │ long paidCount = orders.stream()
          45 │     .filter(o -> "PAID".equals(o.getStatus())).count();

      ⎿  stream scan on 'orders' OrderService.java:38 O(orders)
      ⎿  stream scan on 'orders' (duplicate) OrderService.java:41 O(orders)
      ⎿  stream scan on 'orders' (duplicate) OrderService.java:44 O(orders)
```

## The fix

Combine the scans into a single pass over the collection:

```java
// Before — three traversals of orders: 3 × O(n)
Order highestTotal = orders.stream()
    .max(Comparator.comparing(Order::getTotal)).orElse(null);
Order firstCancelled = orders.stream()
    .filter(o -> "CANCELLED".equals(o.getStatus())).findFirst().orElse(null);
long paidCount = orders.stream()
    .filter(o -> "PAID".equals(o.getStatus())).count();

// After — one traversal: O(n)
Order highestTotal = null;
Order firstCancelled = null;
long paidCount = 0;
for (Order o : orders) {
    if (highestTotal == null || o.getTotal().compareTo(highestTotal.getTotal()) > 0) {
        highestTotal = o;
    }
    if (firstCancelled == null && "CANCELLED".equals(o.getStatus())) {
        firstCancelled = o;
    }
    if ("PAID".equals(o.getStatus())) {
        paidCount++;
    }
}
```

The single-pass version is less elegant — that's the trade-off. You lose the expressiveness of stream pipelines in exchange for doing the work once. Whether that trade-off is worth it depends on the collection size and how many scans you're combining.

!!! tip "You don't always need to combine everything"

    If you have two scans on a small collection, the constant factor is small and the stream version is more maintainable. This pattern matters most when: (a) the collection is large (thousands of elements), (b) the scans are numerous (3+), or (c) the method is called frequently (e.g. per request).

For more on why this scaling behavior matters, see the [Big-O primer](concepts/big-o-primer.md). For the full list of patterns algorilla detects, see the [rules overview](rules/index.md).
