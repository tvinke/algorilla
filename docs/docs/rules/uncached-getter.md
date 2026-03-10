# Uncached Getter

**Rule ID:** `uncached-getter` · **Severity:** INFO · **Complexity:** k·O(lookup) → O(lookup)

## Description

Detects getter-style calls invoked multiple times with the same argument in a function. When a method like `getProduct(id)` is called repeatedly with the same ID, the result should be stored in a local variable.

## Bad Example

```java
public void enrichLineItem(LineItem lineItem) {
    String name = getProduct(lineItem.getProductId()).getName();
    String category = getProduct(lineItem.getProductId()).getCategory(); // Same call
    BigDecimal price = getProduct(lineItem.getProductId()).getPrice();   // Same call again
    // ...
}
```

## Good Example

```java
public void enrichLineItem(LineItem lineItem) {
    Product product = getProduct(lineItem.getProductId());
    String name = product.getName();
    String category = product.getCategory();
    BigDecimal price = product.getPrice();
}
```

## How Detection Works

1. Identifies calls with getter-pattern prefixes: `get`, `find`, `load`, `fetch`, `lookup`, `resolve`
2. Groups by target + method name + argument key
3. Flags groups with 2 or more identical calls
4. Excludes short generic names like `get`, `getOrDefault`

## Suggestion

> Cache the result in a local variable

## Related Rules

- [Redundant Expensive Call](redundant-expensive-call.md) — broader pattern for any repeated call
- [Chained Getters](chained-getters.md) — cascading lookups where one feeds the next
