---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Bulk Load for Single Lookup

**Rule ID:** `bulk-load-for-single-lookup` · **Severity:** WARNING · **Complexity:** O(n) → O(1)

## Description

Detects patterns where all records are loaded from a data source (e.g., `findAll()`, `getAll()`, `list()`) and then immediately filtered in memory to find a single item. This is O(n) when a targeted query could retrieve the item directly in O(1).

## Bad Example

```java
List<Payment> allPayments = paymentRepository.findAll();
Payment target = allPayments.stream()
    .filter(p -> p.getOrderId().equals(orderId))
    .findFirst()
    .orElse(null);
```

## Good Example

```java
Payment target = paymentRepository.findByOrderId(orderId).orElse(null);
```

## Suggestion

> Use a targeted query instead of loading all records and filtering in memory
