---
tags:
  - Java
---

# Java

**Parser:** ANTLR 4 · **File extensions:** `.java` · **Rules:** 23/23

Java has the most mature language support in algorilla, with a full ANTLR-based parser that produces a complete AST.

## What the parser handles

- Full Java syntax up to Java 21 (records, sealed classes, pattern matching)
- Generic type resolution for collection type inference
- Annotation processing (including `@Override`, `@SuppressWarnings`)
- Inner classes and anonymous classes
- Lambda expressions and method references
- Stream API chain detection

## Collection type inference

The Java parser resolves variable types from declarations, helping rules distinguish between O(1) and O(n) operations:

```java
Set<String> ids = new HashSet<>(list);    // contains() is O(1) — not flagged
List<String> ids = new ArrayList<>(list); // contains() is O(n) — flagged
```

When types can't be resolved statically, use [type hints](../getting-started/configuration.md) in `.algorilla.yml`.

## Java-specific patterns

Algorilla recognizes Java-specific APIs and idioms:

- **Stream API chains** — `stream().filter().map().collect()` patterns
- **Iterator patterns** — enhanced for-loops, `forEach`, `Iterator.hasNext()`/`next()`
- **Concurrent utilities** — `CompletableFuture.join()`, `Future.get()`
- **Reflection API** — `Class.getDeclaredMethods()`, `Field.get()`, `getAnnotation()`
- **Parallel streams** — `parallelStream().forEach()` with shared state detection

## Class-qualified method resolution

The parser tracks which class a method belongs to, enabling accurate cross-method analysis:

```java
orderService.processOrder(order);  // resolved to OrderService.processOrder
```

This allows rules like `hidden-nested-loop` to follow calls across files.
