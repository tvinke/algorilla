# Changelog

## 0.1.0 (2026-03-08)

First public release of algorilla.

### Rules

15 built-in rules for detecting algorithmic complexity anti-patterns:

- **nested-lookup** — Linear lookup inside loop body (O(n*m) → O(n+m))
- **sort-for-last** — Sorting entire collection just to get first/last element
- **expensive-sort-comparator** — Linear lookup inside sort comparator
- **date-in-sort** — Date creation/parsing inside sort comparator
- **repeated-linear-scan** — Multiple linear scans on same collection
- **full-scan-for-single-lookup** — Loading all records then filtering in memory
- **heavyweight-object-per-invocation** — ObjectMapper/Gson created in method body
- **repeated-regex-in-loop** — Regex compilation inside loop
- **expensive-serialization-in-loop** — writeValueAsString/readValue inside loop
- **sequential-async-join-in-loop** — Blocking .join()/.get() on futures in loop
- **in-loop-collection-building** — addAll/concat/putAll inside loop
- **n-plus-one-repository-call** — Single-record DAO fetch inside loop
- **redundant-expensive-call** — Same call with same args invoked multiple times
- **uncached-getter** — Getter-pattern call repeated with same argument
- **chained-getters** — Cascading getter chain where each feeds into the next

### Architecture

- **Collection Semantics Registry** — Data-driven YAML-based method classification per language (Java, Groovy, JS/TS, Kotlin). Replaces hardcoded method-name matching with extensible registry.
- **Cross-method analysis** — Rules can follow method references one level deep to detect indirect patterns (e.g. date parsing via helper method in sort comparator).
- **Generalized iteration context** — `findAll{}`, `any{}`, `find{}` with closures now count as iteration context for nested-lookup detection.

### Languages

- Java (ANTLR parser)
- Groovy (ANTLR parser, Groovy-specific GDK method recognition)
- Kotlin (lightweight text-based parser)
- JavaScript / TypeScript / Vue (lightweight text-based parser)

### Output formats

- Console (grouped by file, with evidence chains)
- SARIF v2.1 (GitHub Code Scanning compatible)
- JSON

### Features

- Incremental analysis with content-hash based caching
- Baseline mode for CI (only report new findings)
- Inline suppression via `// algorilla:ignore [rule-id]`
- Custom rules via Kotlin DSL (`.algorilla/rules/*.kts`)
- User-extensible configuration via `.algorilla.yml`
- Auto-detection of project root and source directories
