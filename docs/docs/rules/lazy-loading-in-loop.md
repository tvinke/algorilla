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
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | LOW — heuristic, check manually |
    | **Category** | Query patterns |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n × IO) → O(1 × IO + n) |

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

This rule uses [name-based heuristics](../concepts/how-detection-works.md#name-based-heuristics-low-confidence) — it flags getter methods with plural/collection-style names (`getOrders`, `getLineItems`, `getRoles`) but can't distinguish lazy-loaded collections from preloaded fields without [type information](../concepts/how-detection-works.md#type-resolution-medium-confidence). Scalar getters like `getName()`, `getStatus()` are not flagged.

## What it skips

- Scalar getters (non-plural names)
- Names with scalar-looking suffixes (`-ss`, `-us`, `-ness`, etc.)
- Loops where the entity wasn't assigned from a repository-like fetch
- Non-JVM languages

## Fix

Use a [JPQL fetch join](https://docs.oracle.com/javaee/7/tutorial/persistence-querylanguage005.htm) or [`@EntityGraph`](https://docs.spring.io/spring-data/jpa/reference/jpa/entity-graph.html) to load the association in the same query:

```java
@Query("SELECT o FROM Order o JOIN FETCH o.lineItems")
List<Order> findAllWithLineItems();
```

Or with `@EntityGraph` (Spring Data JPA):

```java
@EntityGraph(attributePaths = "lineItems")
List<Order> findAll();
```
