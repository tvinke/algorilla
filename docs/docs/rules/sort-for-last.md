# Sort for Last

**Rule ID:** `sort-for-last` · **Severity:** WARNING · **Complexity:** O(n log n) → O(n)

## Description

Detects patterns where a collection is sorted only to retrieve the first or last element. Sorting the entire collection to find a single extreme value is O(n log n) when a linear scan with `max()` or `min()` achieves the same result in O(n).

## Bad Example

```java
List<Product> sorted = products.stream()
    .sorted(Comparator.comparing(Product::getPrice))
    .collect(Collectors.toList());
Product cheapest = sorted.get(0); // Sorted just for this
```

## Good Example

```java
Product cheapest = products.stream()
    .min(Comparator.comparing(Product::getPrice))
    .orElse(null);
```

## Suggestion

> Use .max(Comparator.comparing(...)) or .min(...) instead of sorting
