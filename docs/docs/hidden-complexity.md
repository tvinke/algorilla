# The hidden O(n²) problem

Most developers understand that a loop inside a loop is slow. But in real codebases, the inner loop is rarely that obvious. It hides behind method calls, stream operations, and utility functions — easy to miss during code review and hard to notice in tests with small datasets.

This page walks through how that happens, step by step.

## Start simple: loop in a loop

Here's a pattern most developers can spot as inefficient:

```java
for (Order order : orders) {                           // outer loop: runs once per order
    for (String id : discountedProductIds) {           // inner loop: runs once per ID, for EVERY order
        if (id.equals(order.getProductId())) {         // ← this comparison is the "work"
            applyDiscount(order);
            break;
        }
    }
}
```

The outer loop runs `n` times (once per order). For each of those iterations, the inner loop runs up to `m` times (once per discounted product ID). The total number of comparisons is ==n × m==. In computer science, this scaling behavior is described using **Big-O notation** as ==O(n×m)== — or ==O(n²)== when both collections are roughly the same size.[^1]

[^1]: The "O" stands for the *order* of growth: it tells you how the work scales as the input gets larger, ignoring constant factors. See the [Big-O primer](concepts/big-o-primer.md) for a full introduction.

With small inputs, the difference doesn't matter. With production-sized data, it's the difference between milliseconds and minutes:

| Orders (n) | Discounted Product IDs (m) | Comparisons (n × m) |
|--------|-------------|-------------|
| 100 | 100 | 10,000 |
| 1,000 | 1,000 | 1,000,000 |
| 10,000 | 10,000 | 100,000,000 |

No one writes explicit nested loops like this in practice. But the exact same O(n²) behavior hides in code that *looks* clean.

## Step 1: the loop disappears into a method call

On a typical `List` (like `ArrayList`), `contains()` does exactly what the inner loop above does — it walks the list element by element. So this code has the same O(n²) behavior:

```java
for (Order order : orders) {
    if (discountedProductIds.contains(order.getProductId())) {  // hidden linear scan
        applyDiscount(order);
    }
}
```

The inner loop is gone visually, but it's still there. `contains()` on a `List` is ==O(n)== — one comparison per element. A code reviewer sees a single loop with a simple condition. It passes review. It works in the test suite where `discountedProductIds` has 5 entries. Then it ships, and a customer has 15,000 discounted product IDs.

!!! warning "These methods all hide a linear scan"

    Any of these inside a loop creates O(n²). They read like simple checks, which is exactly why they slip through code review.

    | Method | What it does | Complexity |
    |--------|-------------|-----------|
    | `list.contains(x)` | Checks every element | O(n) |
    | `list.indexOf(x)` | Walks until found | O(n) |
    | `stream.filter(...)` | Tests every element | O(n) |
    | `stream.anyMatch(...)` | Tests until match | O(n) worst case |
    | `array.find(...)` (JS) | Walks until found | O(n) |
    | `array.some(...)` (JS) | Tests until match | O(n) worst case |
    | `array.includes(x)` (JS) | Checks every element | O(n) |

## Step 2: the loop hides across methods

Now consider this code, split across two classes:

```java
// OrderService.java
public List<Order> applyDiscounts(List<Order> orders) {
    List<Order> result = new ArrayList<>();
    for (Order order : orders) {
        if (discountService.hasDiscount(order)) {  // what does this cost?
            result.add(order);
        }
    }
    return result;
}
```

```java
// DiscountService.java
public boolean hasDiscount(Order order) {
    return discountedProductIds.contains(order.getProductId());  // O(n) — but who's reading this file?
}
```

Same O(n²). But now the two halves live in different files, possibly different packages. The reviewer of `OrderService.java` sees a clean loop calling a method that returns a boolean. The reviewer of `DiscountService.java` sees a straightforward lookup. Neither sees the combined cost.

!!! danger "This is the pattern that actually ships to production"

    Not the textbook nested loop — the real-world version where complexity is **distributed across modules**. Each file looks fine in isolation. The problem only exists in the *combination*.

## Step 3: layers of indirection

It gets worse. Real applications have layers:

```java
// OrderController.java
public List<OrderDto> getDiscountedOrders(String customerId) {
    List<Order> orders = orderService.getOrders(customerId);
    return orders.stream()
        .filter(order -> discountService.calculateTotal(order))  // O(?)
        .map(this::toDto)
        .collect(Collectors.toList());
}
```

```java
// DiscountService.java
public boolean calculateTotal(Order order) {
    return discountRules.stream()
        .anyMatch(rule -> rule.applies(order));  // O(rules)
}
```

```java
// DiscountRule.java
public boolean applies(Order order) {
    return eligibleCategories.contains(order.getCategory());  // O(categories)
}
```

Three files. Three developers may have written them at different times. The combined complexity: ==O(orders × rules × categories)==. With 1,000 orders, 50 discount rules, and 20 categories, that's 1,000,000 comparisons — for what looks like a simple filter.

!!! info "Why tests don't catch this"

    Unit tests typically use small datasets — 3 orders, 2 discount rules, 1 category. At that scale, O(n³) completes in microseconds. The problem only surfaces in production, where data is 1,000× larger and the code path is 1,000,000× slower.

## What algorilla does about this

Algorilla's static analysis engine parses your source code into an intermediate representation, builds a call graph across files, and follows method calls to detect these hidden patterns — even when they span multiple classes and packages.

Running algorilla on the `OrderService` example from Step 2 would produce:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × discountedProductIds) → O(orders + discountedProductIds)
    com.example.shop.service.OrderService:28

      Linear contains on 'discountedProductIds' inside for-each loop
      → Build a HashSet/Map from 'discountedProductIds' before the loop
      ↳ https://tvinke.github.io/algorilla/rules/nested-lookup

          25 │ List<Order> result = new ArrayList<>();
          26 │ for (Order order : orders) {
          27 │     if (discountService.hasDiscount(order)) {
          28 │         result.add(order);

      ⎿  for-each loop over orders OrderService.java:26 O(orders)
        ⎿  hasDiscount() inside loop OrderService.java:27 O(discountedProductIds)
          ⎿  contains on 'discountedProductIds' DiscountService.java:12 O(discountedProductIds) ← bottleneck
```

!!! tip "Reading the evidence chain"

    The `⎿` tree at the bottom is the key: it traces the path from the loop in `OrderService.java`, through the `hasDiscount()` call, into `DiscountService.java` where it pinpoints the `contains()` as the bottleneck. You don't need to manually follow the call chain — algorilla does it for you.

## The fix

Regardless of how many layers of indirection exist, the fix is the same: convert `discountedProductIds` to a `HashSet` for ==O(1)== lookups.

```java
// Before — contains() on a List is O(n) per iteration → O(n²) total
for (Order order : orders) {
    if (discountedProductIds.contains(order.getProductId())) {
        applyDiscount(order);
    }
}

// After — build a Set once, lookups become O(1) → O(n) total
Set<String> discountedProductIdSet = new HashSet<>(discountedProductIds);
for (Order order : orders) {
    if (discountedProductIdSet.contains(order.getProductId())) {
        applyDiscount(order);
    }
}
```

The example above shows the simplest case — the collection is right there and you can wrap it in a `HashSet`. In practice, `discountedProductIds` might live behind a service boundary, an injected dependency, or a shared API where you can't just swap the type. Common approaches: push the Set conversion into the service that owns the data (so callers get O(1) for free), add a precomputed lookup alongside the existing list, or introduce a method that returns a Set view. The *data structure choice* is always the fix — where you make that choice depends on your architecture. Either way, at least you know it's there now.

For a deeper dive into Big-O notation and the specific complexity classes, see the [Big-O primer](concepts/big-o-primer.md). For the full list of patterns algorilla detects, see the [rules overview](rules/index.md).
