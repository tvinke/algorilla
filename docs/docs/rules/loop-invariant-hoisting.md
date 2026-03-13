# Loop-Invariant Hoisting

**ID:** `loop-invariant-hoisting` · **Category:** Redundancy · **Severity:** INFO

Flags function calls inside loop bodies that don't depend on the loop variable — the result is the same on every iteration, so the call can be hoisted before the loop.

## What it detects

```java
void process(List<Item> items) {
    for (Item item : items) {
        String timeout = config.getTimeout();  // same result every iteration
        item.apply(timeout);
    }
}
```

`config.getTimeout()` doesn't reference `item` or any variable declared inside the loop. It could be called once before the loop.

## What it skips

- Calls that reference the loop variable or any loop-local variable
- IO methods (side effects — calling them once vs N times has different semantics)
- Mutation methods, builder methods, cheap/trivial methods (from YAML)
- Object constructors (already caught by `expensive-construction`)

## Fix

Hoist the call before the loop:

```java
void process(List<Item> items) {
    String timeout = config.getTimeout();
    for (Item item : items) {
        item.apply(timeout);
    }
}
```
