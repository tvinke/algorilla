---
tags:
  - Java
  - Kotlin
  - Groovy
---

# Lazy Loading In Loop

Potential JPA/Hibernate lazy-loading N+1 patterns: collection getter calls on entities inside loops.

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `lazy-loading-in-loop` |
    | **Severity** | WARNING |
    | **Confidence** | LOW |
    | **Category** | Query patterns |
    | **Complexity** | O(n × IO) → O(1 × IO + n) |

## What it detects

```java
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    List<LineItem> items = order.getLineItems();  // lazy load per iteration
    process(items);
}
```

Each `getLineItems()` call may trigger a separate SQL query if the association is lazily loaded.

## Why LOW confidence

This rule uses name-based heuristics — it flags getter methods with plural/collection-style names (`getOrders`, `getLineItems`, `getRoles`) but can't distinguish lazy-loaded collections from preloaded fields without type information. Scalar getters like `getName()`, `getStatus()` are not flagged.

## What it skips

- Scalar getters (non-plural names)
- Names with scalar-looking suffixes (`-ss`, `-us`, `-ness`, etc.)
- Loops where the entity wasn't assigned from a repository-like fetch
- Non-JVM languages

## Fix

Use a fetch join or `@EntityGraph`:

```java
@Query("SELECT o FROM Order o JOIN FETCH o.lineItems")
List<Order> findAllWithLineItems();
```
