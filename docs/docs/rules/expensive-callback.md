---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Expensive Callback

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `expensive-callback` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | LOW — heuristic, check manually |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n × cost) → O(n) |

## Description

Detects expensive operations inside higher-order function callbacks like `filter`, `map`, `forEach`, and `removeIf`. These callbacks execute once per element — an expensive operation inside them multiplies across the entire collection.

## Typical

```java
List<Order> recent = orders.stream()
    .filter(o -> LocalDate.parse(o.getDateStr()).isAfter(cutoff))
    .collect(Collectors.toList());
```

`LocalDate.parse()` is called for every order. Parsing dates is expensive — this creates O(n) date parser invocations where pre-parsing or caching would avoid it.

```java
List<Boolean> flags = items.stream()
    .map(item -> allowed.contains(item))  // linear lookup per item
    .collect(Collectors.toList());
```

`List.contains()` inside `map` makes this O(n × m).

## After

```java
// Pre-parse dates before filtering
Map<Order, LocalDate> parsed = orders.stream()
    .collect(Collectors.toMap(o -> o, o -> LocalDate.parse(o.getDateStr())));
List<Order> recent = orders.stream()
    .filter(o -> parsed.get(o).isAfter(cutoff))
    .collect(Collectors.toList());
```

```java
// Convert to Set for O(1) lookups
Set<String> allowedSet = new HashSet<>(allowed);
List<Boolean> flags = items.stream()
    .map(allowedSet::contains)
    .collect(Collectors.toList());
```

## What it catches

- **Date parsing/creation** — `LocalDate.parse()`, `new Date()`, `SimpleDateFormat.parse()`
- **Regex compilation** — `Pattern.compile()`, `new Regex()`
- **Linear lookups** — `List.contains()`, `indexOf()` on non-Set collections
- **Heavyweight object construction** — `new ObjectMapper()`, `new Gson()`
- **Cross-method patterns** — expensive operations hidden in methods called from callbacks

## Suggestion

> Move expensive operations out of the callback — pre-compute, cache, or use a more efficient data structure
