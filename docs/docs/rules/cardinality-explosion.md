# Cardinality Explosion

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `cardinality-explosion` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n × m) → O(n + m) |

Flags code where the output grows as the **product** of input sizes rather than the sum — nested-loop Cartesian products and flatMap cross joins.

## What it detects

**Nested loops over different collections with mutation:**

```java
for (Order o : orders) {
    for (Product p : products) {
        results.add(new OrderProduct(o, p));  // O(orders × products) output
    }
}
```

**flatMap where the lambda iterates a different collection:**

```java
orders.stream()
    .flatMap(o -> products.stream().map(p -> combine(o, p)))  // O(n×m)
    .collect(toList());
```

## What it skips

- Single loops with mutation (that's just building a list — no explosion)
- Nested loops over the **same** collection (self-join / pair comparison — flagged at LOW/INFO only)
- Variables with names suggesting small/bounded collections (`types`, `statuses`, `config`)

## Subsumption

When this rule and `in-loop-collection-building` both fire at the same location, this rule takes precedence (more specific diagnosis).

## Fix

Use an index or join strategy instead of the full cross product:

```java
Map<String, List<Product>> productsByCategory = products.stream()
    .collect(groupingBy(Product::getCategory));
for (Order o : orders) {
    List<Product> matching = productsByCategory.getOrDefault(o.getCategory(), List.of());
    // O(orders + products) instead of O(orders × products)
}
```
