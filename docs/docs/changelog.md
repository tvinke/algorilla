# Changelog

## 0.1.0 (2026-03-09)

First release of algorilla.

### Rules

15 built-in rules for detecting algorithmic complexity anti-patterns:

| Rule | What it catches |
|------|----------------|
| `nested-lookup` | Linear lookup inside loop body (O(n*m) → O(n+m)) |
| `repeated-linear-scan` | Multiple linear scans on same collection |
| `sort-for-last` | Sorting entire collection just to get first/last element |
| `expensive-sort-comparator` | Linear search, date parsing, or heavy objects in sort comparator |
| `filter-after-sort` | `sorted().filter()` — filtering after sorting wastes effort |
| `bulk-load-for-single-lookup` | `findAll()` + filter when a targeted query would do |
| `expensive-construction` | ObjectMapper/Gson/SimpleDateFormat in method body |
| `n-plus-one-query` | Single-record DAO fetch or countBy inside a loop |
| `repeated-regex-in-loop` | Regex compilation inside loop |
| `expensive-serialization-in-loop` | Serialization/deserialization inside loop |
| `sequential-async-join-in-loop` | Blocking `.join()`/`.get()` on futures in loop |
| `in-loop-collection-building` | `addAll()`/`concat()` inside loop |
| `redundant-expensive-call` | Same call with same args invoked multiple times |
| `uncached-getter` | Getter-pattern call repeated with same argument |
| `chained-getters` | Cascading getter chain where each feeds into the next |

### Languages

- Java (ANTLR parser)
- Groovy (ANTLR parser, Groovy-specific GDK method recognition)
- Kotlin (lightweight text-based parser)
- JavaScript / TypeScript / Vue (lightweight text-based parser)

### Features

- **Rule categories** — Every rule belongs to a category (Loop amplifiers, Sort abuse, Redundancy, Construction cost, Query patterns). Console output shows the category tag per finding.
- **Rule aliases** — Rules declare aliases for backwards compatibility. Old IDs in suppress comments and config keep working after renames.
- **Suggested fixes** — Sort-for-last, expensive-construction, and filter-after-sort include fix suggestions in console and SARIF output.
- **`--language` filter** — `-l` / `--language` option to restrict analysis to specific languages.
- **Cross-method analysis** — Rules follow method references one level deep to detect indirect patterns.
- **Collection Semantics Registry** — YAML-based method classification per language, extensible via config.
- **Method purity model** — Side-effect classification for smarter redundant-call detection.
- Incremental analysis with content-hash based caching
- Baseline mode for CI (only report new findings)
- Inline suppression via `// algorilla:ignore [rule-id]`
- Custom rules via Kotlin DSL (`.algorilla/rules/*.kts`)
- User-extensible configuration via `.algorilla.yml`
- Auto-detection of project root and source directories

### Output formats

- Console (grouped by file, with evidence chains)
- SARIF v2.1 (GitHub Code Scanning compatible)
- JSON

### CI/CD

- GitHub Actions CI (Java 11, 17, 21 matrix)
- Release automation via Release Please
- CI/CD integration guide and pre-commit hook guide in docs
