# Lazy Loading In Loop

**ID:** `lazy-loading-in-loop` · **Category:** Query patterns · **Severity:** WARNING · **Confidence:** LOW

Flags potential JPA/Hibernate lazy-loading N+1 patterns: getter calls on entities inside loops where the getter name suggests a lazy-loaded collection.

## What it detects

```java
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    List<LineItem> items = order.getLineItems();  // lazy load per iteration
    process(items);
}
```

Each `getLineItems()` call may trigger a separate SQL query if the association is lazily loaded — the classic N+1 problem.

## Confidence: LOW

This rule defaults to LOW confidence because it uses name-based heuristics: it flags getter methods with plural/collection-style names (`getOrders`, `getLineItems`, `getRoles`) but can't distinguish a lazy-loaded collection from a pre-loaded field without type information.

Scalar getters like `getName()`, `getStatus()`, `getAddress()` are not flagged.

## What it skips

- Scalar getters (non-plural names)
- Names ending with scalar-looking suffixes (`-ss`, `-us`, `-ness`, etc.)
- Loops where the entity variable wasn't assigned from a repository-like fetch
- Non-JVM languages (rule is Java/Kotlin/Groovy only)

## Fix

Use a fetch join or `@EntityGraph` to eagerly load the association:

```java
@Query("SELECT o FROM Order o JOIN FETCH o.lineItems")
List<Order> findAllWithLineItems();
```

Or batch with an IN clause after collecting the IDs.
