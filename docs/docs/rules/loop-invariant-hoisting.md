---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Loop-Invariant Hoisting

Function calls inside loop bodies that don't depend on the loop variable — the result is the same on every iteration.

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `loop-invariant-hoisting` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | INFO — worth knowing, may not matter at your scale |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | LOW — heuristic, check manually |
    | **Category** | Redundancy |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n × call) → O(call) |

## What it detects

```java
for (Item item : items) {
    String timeout = config.getTimeout();  // same result every iteration
    item.apply(timeout);
}
```

`config.getTimeout()` doesn't reference `item` or any variable declared inside the loop.

## What it skips

- Calls that reference the loop variable or any loop-local variable
- IO methods (side effects)
- Mutation methods, builder methods, cheap/trivial methods
- Object constructors (caught by `expensive-construction`)

## Fix

```java
String timeout = config.getTimeout();
for (Item item : items) {
    item.apply(timeout);
}
```
