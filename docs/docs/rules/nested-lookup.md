# Nested Lookup

**Rule ID:** `nested-lookup` · **Severity:** WARNING · **Complexity:** O(n²) → O(n)

## Description

Detects linear lookup operations (`contains`, `indexOf`, `find`, `filter`, `any`, `some`, `includes`) inside loop bodies. When a collection is searched linearly on every iteration, the combined complexity becomes O(n·m) or O(n²) where O(n) would suffice with a pre-built Set or Map.

## Bad Example

=== "Java"

    ```java
    for (Order order : orders) {
        if (eligibleIds.contains(order.getId())) { // O(n) per iteration
            process(order);
        }
    }
    ```

=== "Groovy"

    ```groovy
    orders.each { order ->
        if (eligibleIds.contains(order.id)) { // O(n) per iteration
            process(order)
        }
    }
    ```

=== "JavaScript"

    ```javascript
    orders.forEach(order => {
        if (eligibleIds.includes(order.id)) { // O(n) per iteration
            process(order);
        }
    });
    ```

## Good Example

=== "Java"

    ```java
    Set<String> eligibleIdSet = new HashSet<>(eligibleIds);
    for (Order order : orders) {
        if (eligibleIdSet.contains(order.getId())) { // O(1) per iteration
            process(order);
        }
    }
    ```

=== "JavaScript"

    ```javascript
    const eligibleIdSet = new Set(eligibleIds);
    orders.forEach(order => {
        if (eligibleIdSet.has(order.id)) { // O(1) per iteration
            process(order);
        }
    });
    ```

## How Detection Works

1. The parser identifies loop constructs (for, while, forEach, each, stream().forEach())
2. Inside each loop body, the rule looks for linear lookup method calls
3. If the target collection is not a known O(1) type (Set, Map, HashMap, etc.), a finding is reported
4. The rule checks parameter types and variable declarations to avoid false positives on Set/Map operations

## Suggestion

> Build a HashSet/Map from '{targetVariable}' before the loop

## Related Rules

- [Repeated Linear Scan](repeated-linear-scan.md) — multiple lookups on the same collection outside loops
