# Algorilla

A static analysis tool that detects algorithmic complexity anti-patterns in source code.

Algorilla identifies places where runtime complexity is unnecessarily high — O(n^2) nested lookups, sort-for-last, expensive sort comparators, repeated linear scans — and provides concrete suggestions to reduce complexity.

## What does it find?

Consider this Java code that looks reasonable at first glance:

```java
void processOrders(List<Order> orders, List<String> priorityIds) {
    for (Order order : orders) {
        if (priorityIds.contains(order.getId())) {  // O(n) lookup on every iteration
            ship(order);
        }
    }
}
```

Algorilla detects that `priorityIds.contains()` performs a linear scan inside a loop, making this O(n*m):

```
src/main/java/com/example/OrderService.java:3:5
  nested-lookup Nested Lookup (O(n*m) → O(n+m))
  Linear contains on 'priorityIds' inside for-each loop
  → Build a HashSet/Map from 'priorityIds' before the loop
  Evidence:
    1. OrderService.java:3  for-each loop over orders       [INSIDE_LOOP]
    2. OrderService.java:4  contains on 'priorityIds'       [INSIDE_LOOP]
```

The fix is simple — convert to a `HashSet` before the loop:

```java
void processOrders(List<Order> orders, List<String> priorityIds) {
    Set<String> prioritySet = new HashSet<>(priorityIds);  // O(m) one-time cost
    for (Order order : orders) {
        if (prioritySet.contains(order.getId())) {  // O(1) lookup
            ship(order);
        }
    }
}
```

## Features

- **Multi-language**: Java, Groovy, Kotlin, JavaScript/TypeScript (including Vue SFC)
- **Cross-file analysis**: Follows call chains across files and languages via call graph construction
- **7 built-in rules**: Covering the most common algorithmic complexity anti-patterns
- **Evidence chains**: Every finding explains *why* the pattern is problematic with a traceable chain
- **SARIF output**: Native support for GitHub Code Scanning and VS Code integration
- **Incremental analysis**: Caches results per file — only re-analyzes changed files
- **Baseline mode**: Track new findings only, ignore known issues
- **Configurable**: `.algorilla.yml` for project-specific rules, type hints, and exclusions
- **Custom rules**: Define your own rules via Kotlin Script DSL
- **Project-aware**: Automatically detects Gradle/Maven structure and excludes test code
- **Deterministic**: No AI/ML — pure pattern matching on AST level, fully reproducible

## Rules

| Rule | Detects | Complexity |
|------|---------|------------|
| `nested-lookup` | find/filter/contains inside loop | O(n*m) → O(n+m) |
| `sort-for-last` | sort() followed by first()/last() | O(n log n) → O(n) |
| `expensive-sort-comparator` | Linear search in sort comparator | O(n^2 log n) → O(n log n) |
| `date-in-sort` | Date parsing in sort comparator | Object churn |
| `repeated-linear-scan` | Same collection scanned multiple times | k*O(n) → O(n) |
| `full-scan-for-single-lookup` | Load-all followed by filter | O(n) → O(1) |
| `heavyweight-object-per-invocation` | new ObjectMapper() in method body | Unnecessary allocation |

## Requirements

- **JDK 21** or later

## Quick Start

```bash
# Build
git clone https://github.com/tvinke/algorilla.git
cd algorilla
./gradlew shadowJar

# Analyze a project
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar src/main

# Analyze with SARIF output
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --format sarif --output report.sarif src/

# JSON output
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --format json src/
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

Save a baseline of current findings, then only report new issues going forward:

```bash
# Save current state as baseline
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --save-baseline .algorilla/baseline.json src/

# Later: only show NEW findings
java -jar cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar --baseline .algorilla/baseline.json src/
```

### Inline Suppression

Suppress specific findings with comments in your source code:

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

This compiles all modules, runs Detekt and ktlint checks, and executes all tests.

### Create Executable JAR

```bash
./gradlew shadowJar
```

The JAR is created at `cli/build/libs/algorilla-0.1.0-SNAPSHOT.jar`.

### Run Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :core:test
./gradlew :lang-java:test
./gradlew :lang-groovy:test
./gradlew :lang-javascript:test
./gradlew :lang-kotlin:test

# Run a specific test class
./gradlew :lang-java:test --tests "com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParserTest"
```

### Code Quality

```bash
# Run Detekt static analysis
./gradlew detekt

# Run ktlint formatting check
./gradlew ktlintCheck

# Auto-fix formatting issues
./gradlew ktlintFormat

# Run everything (compile + test + detekt + ktlint)
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

Algorilla uses a four-pass analysis pipeline:

1. **Parse**: Source files → language-agnostic Intermediate Representation (IR)
2. **Call Graph**: Build caller-callee relationships across all files
3. **Annotate**: Estimate complexity and propagate execution context bottom-up
4. **Evaluate**: Run rules against the annotated IR, produce findings

## Author

Ted Vinke

## License

Apache License 2.0
