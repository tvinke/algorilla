# Expensive Sort Comparator

**Rule ID:** `expensive-sort-comparator` · **Severity:** WARNING · **Category:** Sort abuse · **Complexity:** O(n² log n) → O(n log n)

## Description

Detects expensive operations inside sort comparators. When a comparator performs O(n) work per comparison, the sort becomes O(n² log n) instead of O(n log n). This includes:

- **Linear lookups** — `indexOf`, `contains`, `find`, `filter` on a collection
- **Date parsing** — `LocalDate.parse()`, `new Date()`, `DateTimeFormatter` calls
- **Indirect expensive calls** — helper methods that internally do the above (detected via cross-method analysis)

## Bad Examples

```java
// Linear lookup in comparator
orders.sort((a, b) -> {
    int indexA = paymentMethods.indexOf(a.getPaymentMethod()); // O(n) per comparison
    int indexB = paymentMethods.indexOf(b.getPaymentMethod());
    return Integer.compare(indexA, indexB);
});

// Date parsing in comparator
orders.sort((a, b) -> {
    LocalDate dateA = LocalDate.parse(a.getOrderDate()); // Parsed every comparison
    LocalDate dateB = LocalDate.parse(b.getOrderDate());
    return dateA.compareTo(dateB);
});
```

## Good Examples

```java
// Pre-build lookup map
Map<String, Integer> methodPriority = new HashMap<>();
for (int i = 0; i < paymentMethods.size(); i++) {
    methodPriority.put(paymentMethods.get(i), i);
}
orders.sort(Comparator.comparingInt(order ->
    methodPriority.getOrDefault(order.getPaymentMethod(), Integer.MAX_VALUE)
));

// Pre-parse dates, or compare ISO strings directly
orders.sort(Comparator.comparing(Order::getOrderDate));
```

!!! info "Why this is worse than it looks"
    A sort comparator runs O(n log n) times — not O(n). For 10,000 orders, the comparator is called roughly 130,000 times. If each call does a linear scan or parses a date, the total cost explodes.

## Suggestion

> Build a lookup Map before the sort, or pre-parse expensive values once
