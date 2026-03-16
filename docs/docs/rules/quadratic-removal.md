---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Quadratic Removal

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `quadratic-removal` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n²) → O(n) |

## Description

Detects `remove()` calls on lists inside loops. Each `ArrayList.remove()` shifts all subsequent elements one position, making it O(n) per removal. In a loop, this compounds to O(n²).

This rule excludes `Map.remove()` (O(1) for HashMap), `Set.remove()` (O(1) for HashSet), `Iterator.remove()` (safe), and `removeFirst()`/`removeLast()` (which indicate Queue/Deque usage where removal is O(1)).

## Typical

=== "Java"

    ```java
    for (String cancelledId : cancelledOrderIds) {
        orders.remove(cancelledId);    // Shifts remaining elements each time: O(n) per call
    }
    ```

## After

=== "Java"

    ```java
    Set<String> cancelledSet = new HashSet<>(cancelledOrderIds);
    orders.removeAll(cancelledSet);    // Single pass: O(n)
    ```

=== "Java (alternative)"

    ```java
    orders.removeIf(o -> cancelledOrderIds.contains(o));  // Single pass with Iterator
    ```

=== "Java (filter)"

    ```java
    List<String> remaining = orders.stream()
        .filter(o -> !cancelledOrderIds.contains(o))
        .collect(Collectors.toList());
    ```

## Suggestion

> Use removeAll() with a Set, Iterator.remove(), or filter into a new collection
