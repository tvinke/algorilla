# Changelog

## 0.1.1 (unreleased)

### Features

- **Suggested fixes** — Findings can now carry an optional `SuggestedFix` with a description (and optional old/new text for future diff support). Console output shows `-> Suggested fix: ...` below the finding, and SARIF output includes the `fixes` property. Currently enabled for sort-for-last, expensive-construction, and filter-after-sort.
- **Rule categories** — Every rule now belongs to a category (Loop amplifiers, Sort abuse, Redundancy, Construction cost, Query patterns). Console output prefixes each finding with its category tag, e.g. `[Loop amplifiers]`. Custom DSL rules can set `category` in the builder (defaults to Redundancy).

### New rules

- **filter-after-sort** — Detects `sorted().filter()` in stream pipelines where filtering after sorting wastes O(n log n) on elements that will be discarded. Suggests reordering to filter first.

### CLI

- **`--language` filter** — New `-l` / `--language` option to restrict analysis to specific languages (e.g. `--language java,groovy`). Filters both source files and rules. Accepts comma-separated values or can be repeated.

### Breaking changes

- **date-in-sort merged into expensive-sort-comparator** — The `date-in-sort` rule is now part of `expensive-sort-comparator`, which detects linear lookups, date parsing, and heavyweight object creation inside sort comparators. The old `date-in-sort` ID is accepted as an alias in suppress comments and config.
- **Rule aliases** — Rules can now declare `aliases` for backwards compatibility after renames. Old IDs in `// algorilla:ignore` comments and `.algorilla.yml` config keep working.
- **n-plus-one-repository-call → n-plus-one-query** — Renamed to reflect broader scope (not limited to repository calls). Old ID kept as alias.
- **heavyweight-object-per-invocation → expensive-construction** — Renamed for brevity. Old ID kept as alias.
- **full-scan-for-single-lookup → bulk-load-for-single-lookup** — Renamed to better describe the anti-pattern. Old ID kept as alias.

### Documentation

- **GitHub Pages deployment** — Added GitHub Actions workflow to build and publish docs site on pushes to `docs/`.
- **Pre-commit hook guide** — New guide covering Git hook setup, pre-commit framework integration, and tips for fast incremental checks on staged files.

### Rule improvements

- **n-plus-one-query** — Now detects Spring Data `countBy*` query methods inside loops, and `getXxxByYyyId` patterns without requiring a known repository target (catches Vuex store getter N+1 patterns)
- **redundant-expensive-call** — No longer flags trivially cheap methods like `equals()`, `Character.isDigit()`, `Math.toIntExact()` as redundant. Also excludes test assertion methods (`assertThat`, `assertTrue`, `verify`, `expect`, `should*`) that are intentionally repeated.
- **expensive-construction** — Extended with `DecimalFormat`, `SimpleDateFormat`, `Collator`, `RuleBasedCollator`, `MessageDigest` — all expensive to construct per method call.
- **bulk-load-for-single-lookup** — Excludes DOM/test framework targets (`wrapper.findAll()` from Vue Test Utils no longer triggers false positives)

### Parser improvements

- **Java** — Chained method calls now report location at the method name, not the start of the chain expression. Improves accuracy for rules that depend on source position (sort-for-last, filter-after-sort).
- **JavaScript/TypeScript** — Rewrote regex-based scanner with scope-tracking tree builder. Previously produced flat IR trees with empty children, causing all rules that need parent-child relationships (nested-lookup, N+1, etc.) to miss JS/TS patterns entirely. Now correctly builds nested IR trees from brace-depth tracking.

## 0.1.0 (2026-03-08)

First public release of algorilla.

### Rules

15 built-in rules for detecting algorithmic complexity anti-patterns:

- **nested-lookup** — Linear lookup inside loop body (O(n*m) → O(n+m))
- **sort-for-last** — Sorting entire collection just to get first/last element
- **expensive-sort-comparator** — Linear lookup inside sort comparator
- **date-in-sort** — Date creation/parsing inside sort comparator
- **repeated-linear-scan** — Multiple linear scans on same collection
- **bulk-load-for-single-lookup** — Loading all records then filtering in memory
- **expensive-construction** — ObjectMapper/Gson created in method body
- **repeated-regex-in-loop** — Regex compilation inside loop
- **expensive-serialization-in-loop** — writeValueAsString/readValue inside loop
- **sequential-async-join-in-loop** — Blocking .join()/.get() on futures in loop
- **in-loop-collection-building** — addAll/concat/putAll inside loop
- **n-plus-one-query** — Single-record DAO fetch inside loop
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
