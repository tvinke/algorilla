---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Bulk Load for Single Lookup

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `bulk-load-for-single-lookup` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Query patterns |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n) → O(1) |

## Description

Detects patterns where all records are loaded from a data source (e.g., `findAll()`, `getAll()`, `list()`) and then immediately filtered in memory to find a single item. This is O(n) when a targeted query could retrieve the item directly in O(1).

## Typical

```java
List<Payment> allPayments = paymentRepository.findAll();
Payment target = allPayments.stream()
    .filter(p -> p.getOrderId().equals(orderId))
    .findFirst()
    .orElse(null);
```

## After

```java
Payment target = paymentRepository.findByOrderId(orderId).orElse(null);
```

## Suggestion

> Use a targeted query instead of loading all records and filtering in memory
