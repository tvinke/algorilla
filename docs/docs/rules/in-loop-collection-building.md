---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# In-Loop Collection Building

**Rule ID:** `in-loop-collection-building` · **Severity:** WARNING · **Complexity:** O(n·m) → O(n+m)

## Description

Detects patterns that repeatedly copy or rebuild collections inside loops. Calling `addAll()`, `putAll()`, or `concat()` inside a loop copies elements on every iteration, turning an O(n) loop into O(n·m) due to repeated array/list copying.

## Bad Example

=== "Java"

    ```java
    List<LineItem> allLineItems = new ArrayList<>();
    for (Order order : orders) {
        allLineItems.addAll(order.getLineItems()); // Copies entire list each iteration
    }
    ```

=== "JavaScript"

    ```javascript
    let allLineItems = [];
    orders.forEach(order => {
        allLineItems = allLineItems.concat(order.lineItems); // Creates new array each iteration
    });
    ```

## Good Example

=== "Java"

    ```java
    List<LineItem> allLineItems = orders.stream()
        .flatMap(order -> order.getLineItems().stream())
        .collect(Collectors.toList());
    ```

=== "JavaScript"

    ```javascript
    const allLineItems = orders.flatMap(order => order.lineItems);
    ```

## Suggestion

> Accumulate into a single collection, then call addAll() once after the loop

## Related Rules

- [Nested Lookup](nested-lookup.md) — another pattern where loop bodies cause quadratic behavior
