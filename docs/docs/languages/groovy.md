---
tags:
  - Groovy
---

# Groovy

**Parser:** ANTLR 4 (Java grammar + preprocessor) · **File extensions:** `.groovy` · **Rules:** 23/23

Groovy uses the Java ANTLR grammar with a preprocessing step that normalizes Groovy-specific syntax before parsing.

## What the parser handles

- Groovy closures and GString interpolation
- `def` keyword and dynamic typing
- GDK (Groovy Development Kit) collection methods
- Builder patterns and DSLs
- Grape annotations
- Traits

## GDK method recognition

Algorilla knows about Groovy's extensions to the Java collections API:

```groovy
// Flagged: sort-for-last via GDK
def oldest = people.sort { it.age }.last()

// Better:
def oldest = people.max { it.age }
```

GDK methods like `collect`, `find`, `findAll`, `each`, `eachWithIndex`, `inject`, and `groupBy` are recognized as iteration or lookup operations.

## Closure handling

Groovy closures are treated as lambda equivalents. Rules that detect expensive operations in callbacks work with closures:

```groovy
// Flagged: expensive-callback
items.findAll { LocalDate.parse(it.dateStr).isAfter(cutoff) }
```

## Dynamic typing

Since Groovy is dynamically typed, collection types often can't be inferred statically. Use [type hints](../getting-started/configuration.md) when algorilla reports false positives on `contains()` calls against Sets or Maps:

```yaml
type-hints:
  idSet: HashSet
  lookupMap: HashMap
```
