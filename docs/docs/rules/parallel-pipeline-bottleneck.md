---
tags:
  - Java
  - Kotlin
  - Groovy
---

# Parallel Pipeline Bottleneck

**Rule ID:** `parallel-pipeline-bottleneck` · **Severity:** WARNING · **Category:** Concurrency

*Previously:* `parallel-stream-bottleneck` (still accepted as alias in suppress comments and config)

## Description

Detects `parallelStream().forEach()` where the callback mutates shared state. Mutating a shared collection from parallel threads negates the benefits of parallelism and risks data corruption — `ArrayList`, `HashMap`, and most standard collections are not thread-safe.

## Bad Example

```java
List<String> results = new ArrayList<>();
items.parallelStream().forEach(item ->
    results.add(item.toUpperCase())  // shared mutable state!
);
```

Multiple threads call `results.add()` concurrently on a non-thread-safe `ArrayList`. This can silently lose elements, throw `ConcurrentModificationException`, or corrupt internal state.

## Good Example

```java
List<String> results = items.parallelStream()
    .map(item -> item.toUpperCase())
    .collect(Collectors.toList());
```

`collect()` handles thread-safe accumulation internally. No shared mutable state needed.

## What it catches

- `parallelStream().forEach()` with mutation calls (`add`, `put`, `remove`, `addAll`, `clear`, etc.) on variables defined outside the lambda
- Mutations on the iterated element itself are fine and are not flagged

## Suggestion

> Use `.collect()` or `.reduce()` instead of mutating shared state in `.forEach()`
