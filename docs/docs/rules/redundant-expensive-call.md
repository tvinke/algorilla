---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Redundant Expensive Call

**Rule ID:** `redundant-expensive-call` · **Severity:** INFO · **Complexity:** k·O(f) → O(f)

## Description

Detects the same parameterized method call invoked multiple times with identical arguments within a single function. The result should be computed once and cached in a local variable.

## Bad Example

```java
public void processOrder(Order order) {
    if (calculateDiscount(order.getItems()).compareTo(BigDecimal.ZERO) > 0) {
        applyDiscount(order, calculateDiscount(order.getItems())); // Same call, same args
    }
    log.info("Discount: {}", calculateDiscount(order.getItems())); // Again
}
```

## Good Example

```java
public void processOrder(Order order) {
    BigDecimal discount = calculateDiscount(order.getItems());
    if (discount.compareTo(BigDecimal.ZERO) > 0) {
        applyDiscount(order, discount);
    }
    log.info("Discount: {}", discount);
}
```

## How Detection Works

1. Groups all function calls within a method by their signature (target + method name + argument fingerprint)
2. Flags groups with 2 or more identical calls
3. Excludes trivial methods (`equals`, `hashCode`, `size`, `isEmpty`, `toString`, etc.)
4. Excludes builder methods (`path`, `header`, `param`, `query`, `body`)
5. Excludes methods classified as having side effects

## Suggestion

> Cache the result in a local variable
