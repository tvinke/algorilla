# Algorilla

**Find the hidden O(n^2) in your codebase before your users do.**

Algorilla is a static analysis tool that detects algorithmic complexity anti-patterns in Java, Groovy, Kotlin, and JavaScript/TypeScript code. It spots the kind of performance bugs that pass code review, work fine in tests, and then bring production to its knees when real data hits.

## The Problem

This code looks fine. It passes every code review. It works perfectly in your test suite with 10 items:

```java
void processOrders(List<Order> orders, List<String> priorityIds) {
    for (Order order : orders) {
        if (priorityIds.contains(order.getId())) {
            ship(order);
        }
    }
}
```

But `List.contains()` is O(n) — it scans the entire list on every call. Inside that loop, the real complexity is **O(n * m)**. With 10 orders and 5 priority IDs, that's 50 operations. Nobody notices.

With 10,000 orders and 8,000 priority IDs? That's **80 million comparisons**. Your API timeout just went from 12ms to 4 seconds. Good luck finding that in a stack trace.

**These patterns are everywhere.** They hide inside innocent-looking stream chains, get buried three method calls deep across files, and only surface under production load. By then, you're firefighting instead of shipping.

## What Algorilla finds

Run it on the code above:

```
OrderService.java:2:5
  nested-lookup (O(n*m) → O(n+m))
  Linear contains on 'priorityIds' inside for-each loop
  → Build a HashSet/Map from 'priorityIds' before the loop
  Evidence:
    1. OrderService.java:2  for-each loop over orders       [INSIDE_LOOP]
    2. OrderService.java:3  contains on 'priorityIds'       [INSIDE_LOOP]
```

The fix takes 30 seconds:

```java
void processOrders(List<Order> orders, List<String> priorityIds) {
    Set<String> prioritySet = new HashSet<>(priorityIds);
    for (Order order : orders) {
        if (prioritySet.contains(order.getId())) {  // O(1)
            ship(order);
        }
    }
}
```

80 million operations become 10,000. Same result, three orders of magnitude faster.

## Features

- **Multi-language**: Java, Groovy, Kotlin, JavaScript/TypeScript (including Vue SFC)
- **Cross-file analysis**: Follows call chains across files and languages — catches patterns buried in helper methods
- **Evidence chains**: Every finding shows *exactly* why it's a problem, with a traceable path
- **SARIF output**: Plugs into GitHub Code Scanning and VS Code
- **Incremental**: Caches results per file — re-analyzes only what changed
- **Baseline mode**: Adopt incrementally — suppress existing findings, catch only new ones
- **Configurable**: `.algorilla.yml` for rules, type hints, exclusions
- **Custom rules**: Extend with your own patterns via Kotlin Script DSL
- **Project-aware**: Detects Gradle/Maven layout, auto-excludes test code
- **15 built-in rules**: The most impactful algorithmic anti-patterns, from nested lookups to stream ordering
- **Deterministic**: No AI/ML — pure AST pattern matching, fully reproducible

## Rules

| Rule | What it catches | Impact |
|------|----------------|--------|
| `nested-lookup` | `contains()`/`filter()`/`find()` inside a loop | O(n*m) → O(n+m) |
| `repeated-linear-scan` | Same collection scanned 2+ times in one method | k * O(n) → O(n) |
| `sort-for-last` | `sort()` just to get `first()`/`last()` | O(n log n) → O(n) |
| `expensive-sort-comparator` | Linear search, date parsing, or heavy objects in sort comparator | O(n² log n) → O(n log n) |
| `full-scan-for-single-lookup` | `findAll()` + filter when a targeted query would do | O(n) → O(1) |
| `heavyweight-object-per-invocation` | `new ObjectMapper()` inside a method body | ~1ms allocation per call |
| `n-plus-one-repository-call` | `findById()`/`countBy*` inside a loop | O(n * IO) → O(1 * IO) |
| `repeated-regex-in-loop` | `Pattern.compile()` / `new RegExp()` inside loop | Recompilation per iteration |
| `expensive-serialization-in-loop` | `writeValueAsString()`/`JSON.parse()` inside loop | O(n * serialize) |
| `sequential-async-join-in-loop` | `.join()`/`.get()` on futures in loop | Sequential I/O instead of parallel |
| `in-loop-collection-building` | `addAll()`/`concat()` inside loop | O(n*m) copying per iteration |
| `redundant-expensive-call` | Same call with same args invoked multiple times | k * O(f) → O(f) |
| `uncached-getter` | `getXxx(id)` called repeatedly with same argument | Cache in local variable |
| `chained-getters` | Cascading getter chain: `a=get(x)` → `b=get(a)` → `c=get(b)` | Compound lookup cost |
| `filter-after-sort` | `sorted().filter()` — filtering after sorting wastes sort effort | O(n log n) → O(k log k) |

## Requirements

- **JDK 21** or later

## Quick Start

```bash
git clone https://github.com/tvinke/algorilla.git
cd algorilla
./gradlew shadowJar

# Scan a project
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar src/main

# SARIF for CI integration
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --format sarif --output report.sarif src/
```

## Usage

```
Usage: algorilla [OPTIONS] [PATHS...]

Options:
  --format <console|sarif|json>    Output format (default: console)
  --output <file>                  Write output to file instead of stdout
  --severity <error|warning|info>  Minimum severity to report
  --config <file>                  Path to .algorilla.yml config file
  --verbose                        Enable debug logging
  --no-cache                       Disable incremental caching
  --baseline <file>                Filter against baseline file
  --save-baseline <file>           Save current findings as baseline
  --include-tests                  Include test source directories
  --help                           Show help message
```

### Exit Codes

| Code | Meaning |
|------|---------|
| 0 | No findings |
| 1 | Findings detected |
| 2 | Error (parse failure, config error) |

### Baseline Workflow

Adopt Algorilla in an existing project without fixing everything at once:

```bash
# Capture current state
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --save-baseline .algorilla/baseline.json src/

# From now on, only fail on NEW findings
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --baseline .algorilla/baseline.json src/
```

### Inline Suppression

Suppress specific findings with a comment:

```java
// algorilla:ignore nested-lookup
for (Item item : items) {
    if (targets.contains(item.getId())) { ... }
}
```

## Configuration

Create `.algorilla.yml` in your project root:

```yaml
rules:
  nested-lookup:
    enabled: true
    severity: error
  heavyweight-object-per-invocation:
    enabled: false

exclude:
  - "**/generated/**"
  - "**/legacy/**"

heavyweight-types:
  - ObjectMapper
  - Gson
  - DocumentBuilderFactory
```

## Building from Source

### Prerequisites

- JDK 21
- Gradle 8.12+ (wrapper included)

### Build

```bash
./gradlew build
```

Compiles all modules, runs Detekt and ktlint checks, and executes all tests.

### Create Executable JAR

```bash
./gradlew shadowJar
```

Creates `cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar`.

### Run Tests

```bash
# All tests
./gradlew test

# Single module
./gradlew :core:test
./gradlew :lang-java:test
./gradlew :lang-groovy:test
./gradlew :lang-javascript:test
./gradlew :lang-kotlin:test

# Specific test class
./gradlew :lang-java:test --tests "com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParserTest"
```

### Code Quality

```bash
# Static analysis
./gradlew detekt

# Formatting check
./gradlew ktlintCheck

# Auto-fix formatting
./gradlew ktlintFormat

# Everything: compile + test + detekt + ktlint
./gradlew check
```

## Project Structure

```
algorilla/
├── build-logic/          Convention plugins for shared build configuration
├── core/                 IR model, rule engine, call graph, caching, baseline
├── lang-java/            Java parser (ANTLR)
├── lang-groovy/          Groovy parser (Java grammar + preprocessor)
├── lang-kotlin/          Kotlin parser (Java grammar + preprocessor)
├── lang-javascript/      JS/TS/Vue parser (regex-based scanner)
├── reporting/            Console, SARIF, and JSON reporters
├── cli/                  Picocli CLI, config loading, project structure detection
└── docs/                 MkDocs Material documentation site
```

### Architecture

Four-pass analysis pipeline:

1. **Parse** — Source files → language-agnostic Intermediate Representation (IR)
2. **Call Graph** — Build caller-callee relationships across all files and languages
3. **Annotate** — Estimate complexity per function, propagate execution context bottom-up
4. **Evaluate** — Run rules against annotated IR, produce findings with evidence chains

## Author

Ted Vinke

## License

Apache License 2.0
