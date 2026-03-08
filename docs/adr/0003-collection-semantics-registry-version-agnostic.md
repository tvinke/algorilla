# ADR-0003: Collection Semantics Registry — version-agnostic union approach

## Status

Accepted

## Context

Algorilla needs to classify collection method calls (iteration, lookup, sort, access, etc.) to detect algorithmic complexity anti-patterns. Previously, this classification was hardcoded in `when` blocks in `MethodClassification.kt` and `GroovyIRVisitor.kt`. This approach doesn't scale: adding a new method requires code changes, and different languages duplicate classification logic.

We need a data-driven registry that:
- Supports multiple languages with different method names for the same semantics
- Is extensible by users for framework-specific methods
- Covers all relevant collection methods across language versions

## Decision

Seed each language's registry with a **union of all collection methods across all versions** of that language. Do not discriminate by language version.

### Scope per language

| Language | Seed source | Covers |
|----------|-------------|--------|
| Java | `java.util` collections + Stream API | Java 8+ (stable since 1.2 for collections, 8 for streams) |
| Groovy | GDK methods from `DefaultGroovyMethods` | Groovy 2.x–5.x |
| JavaScript/TS | `Array.prototype` + `Set`/`Map` methods | ES5 + ES2015 + ES2016 (universally available) |
| Kotlin | `kotlin.collections` stdlib | Kotlin 1.0+ |

### User extension

Framework-specific methods (Guava, Vavr, Eclipse Collections, Lodash, etc.) are NOT shipped in the default registry. Users extend via `.algorilla.yml`:

```yaml
collection-semantics:
  - type: "com.google.common.collect.ImmutableList"
    linear-lookup: [contains, indexOf]
    iteration: [forEach, stream]
```

## Rationale

- Big-O characteristics are defined by the **data structure**, not the language version. `List.contains()` is O(n) whether it's Java 8 or Java 21. Groovy's `find{}`, `any{}`, `collect{}` have had the same semantics since Groovy 2.x through 5.x.
- If a method doesn't exist in an older version, the source code using it wouldn't compile — algorilla would never encounter it in a scan.
- Semantic changes to collection methods across versions are extremely rare (no known examples for Java, Groovy, JS, or Kotlin stdlib).
- Versioning per language would add complexity (detecting runtime version, maintaining multiple YAML variants) with near-zero practical benefit.

## Consequences

- Adding support for a new method = adding a line to a YAML file
- No version detection logic needed in parsers
- False positives from version mismatch are theoretically possible but practically zero
- Framework coverage relies on user configuration — acceptable for 0.1.0, could ship framework packs later
