---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
  - performance
  - cross-file
description: "Detects O(n²) complexity hidden behind method calls — when a loop calls a method that internally contains another loop, the quadratic cost is invisible at the call site."
---

# Hidden Nested Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `hidden-nested-loop` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n × m) → O(n + m) |

## The problem

A loop that calls a method, and that method contains a loop of its own. The outer loop is O(n), the inner method's loop is O(m), and the combined cost is O(n × m) — but at the call site it looks like a simple O(n) traversal. The quadratic behavior is completely invisible to anyone reading `processOrders()`.

This is one of the hardest performance patterns to catch in code review. The reviewer sees a clean, well-factored method that delegates to a helper. Nothing looks wrong. The complexity is distributed across two methods — possibly in different files — and only Algorilla's cross-method analysis connects them.

It's also the pattern that produces the biggest "oh, _that's_ why it's slow" moments. A loop over departments that calls `summarize()` looks harmless until you realize `summarize()` iterates over every employee in that department, and you have 500 departments with 200 employees each.

## Where this shows up

- In service methods that loop over parent entities and call a helper that loops over children
- In report generators that iterate over sections, where each section method iterates over rows
- In validation pipelines where `validate(order)` internally loops over all line items
- In tree/graph processing where `processNode()` iterates over children (when not recursive)
- In test fixtures that build nested data structures by iterating inside builder methods

## The pattern

```java
public void processOrders(List<Order> orders) {
    for (Order order : orders) {
        validateItems(order);  // (1)!
    }
}

private void validateItems(Order order) {
    for (Item item : order.getItems()) { // (2)!
        checkInventory(item);
    }
}
```

1. Looks like an O(1) call — just validating one order
2. Hidden loop — O(items per order) per invocation

The outer loop is O(orders), the inner loop is O(items per order). Total: O(orders × items). But at the call site in `processOrders`, it reads like a simple O(n) loop.

Algorilla's output makes the hidden cost explicit:

```
warning  · hidden-nested-loop · O(orders × order.getItems())

  validateItems() contains a for-each loop — hidden O(n²) complexity

  ⎿  for-each loop over orders   O(orders)
    ⎿  validateItems() called per iteration
      ⎿  for-each loop inside validateItems()   O(order.getItems()) ← bottleneck
```

## The fix

If the nested iteration is necessary, make the cost visible and batch where possible:

=== "Flatten + batch"

    ```java
    public void processOrders(List<Order> orders) {
        List<Item> allItems = orders.stream()
            .flatMap(o -> o.getItems().stream())
            .collect(Collectors.toList());
        checkInventoryBatch(allItems); // single bulk call, O(n + m)
    }
    ```

=== "Accept the cost (make it visible)"

    ```java
    public void processOrders(List<Order> orders) {
        // O(orders × items) — intentional, each order's items validated independently
        // algorilla:ignore hidden-nested-loop
        for (Order order : orders) {
            validateItems(order);
        }
    }
    ```

Sometimes the nested loop is genuinely necessary — parent-child relationships where each child needs its parent's context. The right fix then is to acknowledge the cost, not hide it.

## Suggestion variants

=== "Freeform"
    > Extract the inner loop into a batch operation, or make the O(n²) cost visible at the call site

    This rule always emits a freeform suggestion since the fix depends heavily on the specific business logic involved.

## When to ignore this

Not every hidden nested loop is a problem. Suppress when:

- The outer and inner collections are both bounded to small sizes (< 100 items each)
- The inner loop does genuinely per-parent work that can't be batched (e.g., computing a per-order subtotal from line items)
- The method call is intentionally factored out for readability and the O(n²) cost is acceptable for the data volumes involved

## What it skips

To reduce false positives, the rule excludes:

- **Recursive methods** — tree walkers and recursive traversals are intentional O(n) over tree nodes
- **String/byte processing** — methods like `encode`, `parse`, `split` that iterate over characters internally
- **Collection copy operations** — `addAll`, `putAll`, `containsAll`
- **Framework internals** — `equals`, `hashCode`, `toString`, `compareTo`

## Related rules

**Loop overhead cluster** — other patterns where the per-iteration cost is higher than it looks:

- [Expensive Callback](expensive-callback.md) — expensive operations inside higher-order function callbacks
- [IO In Loop](io-in-loop.md) — when the hidden cost is I/O latency, not CPU work

**Lookup cluster** — when the hidden loop is a linear search:

- [Nested Lookup](nested-lookup.md) — `contains()` on a List inside a loop (a specific, fixable case of hidden iteration)

**Language-specific examples:**

- [Java](../languages/java.md#the-method-call-that-hides-a-loop) — cross-method analysis tracing through helpers
- [Groovy](../languages/groovy.md#collect--with-an-inner-loop) — `.collect {}` with hidden inner iteration

## Configuration

Disable globally:

```yaml
rules:
  hidden-nested-loop:
    enabled: false
```

Suppress inline:

```java
// algorilla:ignore hidden-nested-loop
for (Order order : orders) {
    validateItems(order);
}
```
