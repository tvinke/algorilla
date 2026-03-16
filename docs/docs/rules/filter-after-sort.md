---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Filter After Sort

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `filter-after-sort` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | INFO — worth knowing, may not matter at your scale |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | HIGH — structurally proven |
    | **Category** | Sort abuse |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n log n + k) → O(k log k + n) |

## Description

Detects stream or collection pipelines where `filter()` comes after `sorted()`. Sorting first processes all N elements at O(n log n), then filter discards some. Filtering first reduces the input to k elements, making the sort O(k log k) where k ≤ n.

## Typical

```java
List<Order> paidByTotal = orders.stream()
    .sorted(Comparator.comparing(Order::getTotal).reversed())
    .filter(o -> "PAID".equals(o.getStatus()))  // Wasted effort: sorted items that get filtered out
    .collect(Collectors.toList());
```

## After

```java
List<Order> paidByTotal = orders.stream()
    .filter(o -> "PAID".equals(o.getStatus()))  // Filter first: fewer items to sort
    .sorted(Comparator.comparing(Order::getTotal).reversed())
    .collect(Collectors.toList());
```

## How Detection Works

1. Looks for `SortCall` nodes followed by `LookupCall(FILTER)` in the same pipeline chain
2. Works for both Java streams and collection method chains

## Suggestion

> Move filter() before sorted() to sort fewer elements
