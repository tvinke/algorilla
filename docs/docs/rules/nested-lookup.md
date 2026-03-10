# Nested Lookup

**Rule ID:** `nested-lookup` · **Severity:** WARNING · **Complexity:** O(n²) → O(n)

## Description

Detects linear lookup operations (`contains`, `indexOf`, `find`, `filter`, `any`, `some`, `includes`) inside loop bodies. When a collection is searched linearly on every iteration, the combined complexity becomes O(n·m) or O(n²) where O(n) would suffice with a pre-built Set or Map.

## Bad Example

=== "Java"

    ```java
    for (Order order : orders) {
        if (discountedProductIds.contains(order.getProductId())) { // O(n) per iteration
            applyDiscount(order);
        }
    }
    ```

=== "Groovy"

    ```groovy
    orders.each { order ->
        if (discountedProductIds.contains(order.productId)) { // O(n) per iteration
            applyDiscount(order)
        }
    }
    ```

=== "JavaScript"

    ```javascript
    orders.forEach(order => {
        if (discountedProductIds.includes(order.productId)) { // O(n) per iteration
            applyDiscount(order);
        }
    });
    ```

## Good Example

=== "Java"

    ```java
    Set<String> discountedProductIdSet = new HashSet<>(discountedProductIds);
    for (Order order : orders) {
        if (discountedProductIdSet.contains(order.getProductId())) { // O(1) per iteration
            applyDiscount(order);
        }
    }
    ```

=== "JavaScript"

    ```javascript
    const discountedProductIdSet = new Set(discountedProductIds);
    orders.forEach(order => {
        if (discountedProductIdSet.has(order.productId)) { // O(1) per iteration
            applyDiscount(order);
        }
    });
    ```

!!! tip "The fix is almost always the same"
    Convert the lookup target to a `HashSet` (or `Map` if you need the value) **before** the loop. This turns O(n) per lookup into O(1), and the overall loop from O(n²) to O(n).

## How Detection Works

1. The parser identifies loop constructs (for, while, forEach, each, stream().forEach())
2. Inside each loop body, the rule looks for linear lookup method calls
3. If the target collection is not a known O(1) type (Set, Map, HashMap, etc.), a finding is reported
4. The rule checks parameter types and variable declarations to avoid false positives on Set/Map operations

## Suggestion

> Build a HashSet/Map from '{targetVariable}' before the loop

## Related Rules

- [Repeated Linear Scan](repeated-linear-scan.md) — multiple lookups on the same collection outside loops
