# Changelog

## 0.3.0 (unreleased)

### Breaking changes

**`--fail-on` default changed from `info` to `warning`.** If your CI relied on info-level findings triggering a non-zero exit, add `--fail-on info` explicitly. The GitHub Action default changed too.

### API stability

This release prepares algorilla for public use by locking down the tool's public surface:

- JSON output now includes `schemaVersion` (currently `1`) and `algorillaVersion` fields
- Baseline and ignore-list files now include a `version` field
- Config parsing is lenient — unknown YAML keys are silently ignored for forward compatibility
- New [Stability & Compatibility](stability.md) page documents what's stable, what's experimental, and the deprecation policy
- `--list-rules` shows a stability tier per rule

### Rule renames

- **`parallel-stream-bottleneck` → `parallel-pipeline-bottleneck`** — language-neutral ID; the old ID is kept as an alias
- **`implicit-regex-in-loop` → `regex-recompilation-in-loop`** — language-neutral ID; "implicit" was Java-specific. No alias — update suppress comments and `.algorilla.yml` configs
- **`multi-pass-stream-fusion` → `repeated-collection-iteration`** — language-neutral ID; "stream fusion" is FP/JVM jargon. No alias — update suppress comments and `.algorilla.yml` configs

### Smarter detection

- **Framework semantics overlays** — method classification is now loaded from per-framework YAML packs (Spring, Guava, etc.), replacing hardcoded constants
- **Exhaustive stdlib coverage** — collection semantics expanded across Java, Kotlin, Groovy, and JS/TS
- **JS/TS type inference** — nested-lookup now infers variable types from initializers, cutting false positives on Set/Map usage
- **Fewer JS/TS false positives** — `regex-recompilation` no longer fires for plain string arguments; small inline array lookups treated as O(1)
- **minSeverity filter** now applied correctly; file groups in console output ordered properly

### Other

- GitHub Action users can now use `@v0` (floating tag) for auto-updates within the 0.x line
- Language tags on rule docs pages
- Docs navigation restructured with horizontal tabs
- Added issue templates, PR template, security policy, and code of conduct
- npm publish is now idempotent on re-tag

## 0.2.0 (2026-03-10)

### New rules

8 new rules, bringing the total to 23:

| Rule | What it catches |
|------|----------------|
| `string-concat-in-loop` | String concatenation inside loop (quadratic copying) |
| `quadratic-removal` | Removing elements from a list inside a loop |
| `repeated-reflection-in-loop` | Reflection calls (getMethod, getField) inside loop |
| `hidden-nested-loop` | Method call inside loop that itself iterates |
| `expensive-callback` | Heavy operations (regex, lookups, nested iteration) inside callbacks |
| `regex-recompilation-in-loop` | String.matches()/replaceAll() inside loop (hidden regex compilation) |
| `parallel-stream-bottleneck` | Shared mutable state inside parallelStream().forEach() |
| `expensive-sort-comparator` | Linear search, date parsing, or heavy work in sort comparator |

### Distribution

Algorilla is now available through 5 channels:

- **npm** — `npx algorilla .` (bundled JRE download)
- **Gradle plugin** — `plugins { id("io.github.tvinke.algorilla") version "0.2.0" }`
- **Docker** — `docker run --rm -v "$(pwd):/src" ghcr.io/tvinke/algorilla /src`
- **GitHub Action** — `uses: tvinke/algorilla@v0.2.0`
- **JAR** — direct download from GitHub Releases

### Features

- **`--list-rules`** — print all available rules and exit
- **`--rule`** — run only specific rules
- **`--fail-on`** — set minimum severity for non-zero exit code
- **`--accept`** — accept specific findings to suppress them going forward
- **Cross-method analysis** — sort-for-last and expensive-callback now follow call chains across methods
- **Type-aware symbol table** — cross-method resolution uses type information for more accurate detection
- **Multi-module Gradle support** — auto-discovers all submodule source roots
- **Findings sorted by severity** — most actionable findings appear first

### Improvements

- Moved cheap-methods and sequential-read-methods to YAML registry (extensible via config)
- Widened N+1 detection to catch `findByX` patterns without suffix whitelist
- Generalized expensive-callback to catch regex, lookups, and nested iteration
- Tightened FP filters across hidden-nested-loop, redundant-call, and purity model
- Skipped recursive methods in hidden-nested-loop detection
- Extracted shared block analysis utilities

### Infrastructure

- Multi-channel release pipeline (GitHub Release, npm, Gradle Plugin Portal, Docker, GitHub Action)
- Dedicated per-rule integration test classes
- Auto-deploy docs on push to main

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
