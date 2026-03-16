# Big-O primer

Big-O notation describes how the execution time (or memory usage) of an algorithm scales with input size. It's the standard way to talk about algorithmic efficiency — not measuring exact milliseconds, but characterizing the *growth rate* as the data gets larger.

For a practical walkthrough of how O(n²) hides in real code, see [the hidden O(n²) problem](../hidden-complexity.md).

## The idea in one sentence

Big-O answers the question: **"If I double the input, how much more work does the code do?"**

- **O(1)** — the same amount of work, regardless of input size
- **O(n)** — double the input, double the work
- **O(n²)** — double the input, quadruple the work

The "O" stands for *order* — as in "order of growth." The notation ignores constant factors and lower-order terms, focusing only on what dominates at scale. `O(2n)` and `O(3n)` are both just `O(n)` because they all grow linearly.

## Common complexities

| Notation | Name | Example | 1,000 items | 1,000,000 items |
|----------|------|---------|-------------|-----------------|
| O(1) | Constant | HashMap lookup | 1 op | 1 op |
| O(log n) | Logarithmic | Binary search | ~10 ops | ~20 ops |
| O(n) | Linear | Single loop | 1,000 ops | 1,000,000 ops |
| O(n log n) | Linearithmic | Merge sort, Timsort | ~10,000 ops | ~20,000,000 ops |
| O(n²) | Quadratic | Nested loops | 1,000,000 ops | 1,000,000,000,000 ops |

!!! note "These are approximations"
    The "ops" column uses rough approximations. For example, log₂(1,000) ≈ 10 and log₂(1,000,000) ≈ 20. The exact numbers don't matter — what matters is the *shape* of the growth curve.

## Why it matters

The difference between O(n) and O(n²) is negligible at small scale but grows fast:

```
Items    O(n)        O(n²)            Ratio
100      100         10,000           100×
1,000    1,000       1,000,000        1,000×
10,000   10,000      100,000,000      10,000×
100,000  100,000     10,000,000,000   100,000×
```

!!! danger "This is why tests don't catch it"
    Your test suite uses 5-10 items. At that scale, O(n²) takes microseconds. In production, with 10,000+ items, the same code takes seconds or minutes. The algorithm didn't change — the data did.

## How to recognize it in code

### O(1) — constant time

Work that doesn't depend on input size at all:

```java
map.get(key);              // HashMap lookup: O(1) amortized
set.contains(value);       // HashSet membership: O(1) amortized
array[index];              // Direct array access: O(1)
```

### O(n) — linear time

A single pass over the data:

```java
for (Order order : orders) {       // O(n): visits each order exactly once
    total += order.getAmount();
}
```

### O(n log n) — linearithmic time

The cost of a good sorting algorithm. Each element is touched `log n` times:

```java
Collections.sort(orders);          // O(n log n): Timsort
orders.stream().sorted();          // O(n log n): same
```

### O(n²) — quadratic time

A loop inside a loop, or any pattern where each element causes a linear scan:

```java
for (Order order : orders) {                                    // O(n)
    if (discountedProductIds.contains(order.getProductId())) {  // O(m) each time → total O(n×m)
        applyDiscount(order);
    }
}
```

!!! tip "O(n×m) vs O(n²)"
    Strictly, a loop of size `n` containing a scan of size `m` is O(n×m). When `n` and `m` are similar in size, this simplifies to O(n²). Algorilla reports both forms depending on whether it can identify the two collections separately.

## Patterns algorilla detects

### O(n²) from nested lookup

```java
// O(n) × O(m) = O(n·m) ≈ O(n²) when collections are similar size
for (Order order : orders) {                                    // O(n)
    if (discountedProductIds.contains(order.getProductId())) {  // O(m) each time
        applyDiscount(order);
    }
}
```

Fix: convert `discountedProductIds` to a `HashSet` before the loop → O(n + m) ≈ O(n).

### O(n² log n) from expensive sort comparator

```java
// The comparator runs O(n log n) times — if it does O(n) work per call, total is O(n² log n)
orders.sort((a, b) -> {
    int indexA = paymentMethods.indexOf(a.getPaymentMethod());  // O(m) per call
    int indexB = paymentMethods.indexOf(b.getPaymentMethod());  // O(m) per call
    return Integer.compare(indexA, indexB);
});
```

Fix: build a lookup map before sorting → comparator becomes O(1), total O(n log n).

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
List<Payment> all = paymentRepository.findAll();  // O(n) database + memory
Payment p = all.stream()
    .filter(x -> x.getOrderId().equals(orderId))
    .findFirst().orElse(null);                    // O(n) scan
```

Fix: use `paymentRepository.findByOrderId(orderId)` → O(1) indexed lookup.

## Further reading

- [Wikipedia: Big O notation](https://en.wikipedia.org/wiki/Big_O_notation) — formal definition and mathematical background
- [Wikipedia: Time complexity](https://en.wikipedia.org/wiki/Time_complexity) — overview of complexity classes with examples
- [Big-O Cheat Sheet](https://www.bigocheatsheet.com/) — visual comparison of common data structure and algorithm complexities
