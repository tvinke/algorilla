# Sort for Last

**Rule ID:** `sort-for-last` · **Severity:** WARNING · **Complexity:** O(n log n) → O(n)

## Description

Detects patterns where a collection is sorted only to retrieve the first or last element. Sorting the entire collection to find a single extreme value is O(n log n) when a linear scan with `max()` or `min()` achieves the same result in O(n).

## Bad Example

```java
List<Order> sorted = orders.stream()
    .sorted(Comparator.comparing(Order::getTotal).reversed())
    .collect(Collectors.toList());
Order highestValue = sorted.get(0); // Sorted just for this
```

## Good Example

```java
Order highestValue = orders.stream()
    .max(Comparator.comparing(Order::getTotal))
    .orElse(null);
```

## Suggestion

> Use .max(Comparator.comparing(...)) or .min(...) instead of sorting
