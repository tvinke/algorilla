# Big-O Primer

Big-O notation describes how the execution time of an algorithm scales with input size.

## Common Complexities

| Notation | Name | Example | 1,000 items | 1,000,000 items |
|----------|------|---------|-------------|-----------------|
| O(1) | Constant | HashMap lookup | 1 op | 1 op |
| O(log n) | Logarithmic | Binary search | 10 ops | 20 ops |
| O(n) | Linear | Single loop | 1,000 ops | 1,000,000 ops |
| O(n log n) | Linearithmic | Good sort | 10,000 ops | 20,000,000 ops |
| O(n²) | Quadratic | Nested loops | 1,000,000 ops | 1,000,000,000,000 ops |

## Why It Matters

The difference between O(n) and O(n²) is invisible at small scale but devastating at production scale:

```
Items    O(n)        O(n²)
100      100         10,000
1,000    1,000       1,000,000
10,000   10,000      100,000,000     ← starts to hurt
100,000  100,000     10,000,000,000  ← timeout
```

## Patterns Algorilla Detects

### O(n²) from nested lookup

```java
// O(n) × O(m) = O(n·m) ≈ O(n²) when collections are similar size
for (Order order : orders) {                    // O(n)
    if (items.contains(order.getItemId())) {    // O(m) each time
        process(order);
    }
}
```

Fix: convert `items` to a `HashSet` before the loop → O(n + m) ≈ O(n).

### O(n log n) where O(n) suffices

```java
// Sorting everything just to get the minimum
Collections.sort(prices);      // O(n log n)
return prices.get(0);          // O(1)
```

Fix: use `Collections.min(prices)` → O(n).

### O(n) where O(1) suffices

```java
// Loading all records to find one
List<User> all = repo.findAll();  // O(n) database + memory
User u = all.stream()
    .filter(x -> x.getId() == id)
    .findFirst().orElse(null);    // O(n) scan
```

Fix: use `repo.findById(id)` → O(1) indexed lookup.
