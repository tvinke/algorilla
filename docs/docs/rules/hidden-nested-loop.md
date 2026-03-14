---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Hidden Nested Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `hidden-nested-loop` |
    | **Severity** | WARNING |
    | **Confidence** | MEDIUM |
    | **Category** | Loop amplifiers |
    | **Complexity** | O(n × m) → O(n + m) |

## Description

Detects loops hidden behind method calls. When a loop calls a method that internally contains another loop, the combined complexity is O(outer × inner) but appears as O(n). The quadratic behavior is invisible at the call site.

Algorilla resolves the called method via cross-method analysis and checks whether its body contains a loop.

## Typical

```java
public void processOrders(List<Order> orders) {
    for (Order order : orders) {
        validateItems(order);  // hides a loop
    }
}

private void validateItems(Order order) {
    for (Item item : order.getItems()) {
        checkInventory(item);
    }
}
```

The outer loop is O(orders), the inner loop is O(items per order). Total: O(orders × items). But at the call site in `processOrders`, it looks like a simple O(n) loop.

## After

If the nested iteration is necessary, make the cost visible and consider batching:

```java
public void processOrders(List<Order> orders) {
    List<Item> allItems = orders.stream()
        .flatMap(o -> o.getItems().stream())
        .collect(Collectors.toList());
    checkInventoryBatch(allItems);  // single bulk call
}
```

## What it skips

To reduce false positives, the rule excludes:

- **Recursive methods** — tree walkers and recursive traversals are intentional
- **String/byte processing** — methods like `encode`, `parse`, `split` that iterate over characters
- **Collection copy operations** — `addAll`, `putAll`, `containsAll`
- **Framework internals** — `equals`, `hashCode`, `toString`, `compareTo`

## Suggestion

> Extract the inner loop into a batch operation, or make the O(n²) cost visible at the call site
