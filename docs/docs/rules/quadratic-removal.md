# Quadratic Removal

**Rule ID:** `quadratic-removal` · **Severity:** WARNING · **Complexity:** O(n²) → O(n)

## Description

Detects `remove()` calls on lists inside loops. Each `ArrayList.remove()` shifts all subsequent elements one position, making it O(n) per removal. In a loop, this compounds to O(n²).

This rule excludes `Map.remove()` (O(1) for HashMap), `Set.remove()` (O(1) for HashSet), `Iterator.remove()` (safe), and `removeFirst()`/`removeLast()` (which indicate Queue/Deque usage where removal is O(1)).

## Bad Example

=== "Java"

    ```java
    for (String extinct : extinctSpecies) {
        animals.remove(extinct);       // Shifts remaining elements each time: O(n) per call
    }
    ```

## Good Example

=== "Java"

    ```java
    Set<String> extinctSet = new HashSet<>(extinctSpecies);
    animals.removeAll(extinctSet);     // Single pass: O(n)
    ```

=== "Java (alternative)"

    ```java
    animals.removeIf(a -> extinctSpecies.contains(a));  // Single pass with Iterator
    ```

=== "Java (filter)"

    ```java
    List<String> remaining = animals.stream()
        .filter(a -> !extinctSpecies.contains(a))
        .collect(Collectors.toList());
    ```

## Suggestion

> Use removeAll() with a Set, Iterator.remove(), or filter into a new collection
