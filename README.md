# Algorilla

A static analysis tool that detects algorithmic complexity anti-patterns in source code.

Algorilla identifies places where runtime complexity is unnecessarily high — O(n^2) nested lookups, sort-for-last, expensive sort comparators, repeated linear scans — and provides concrete suggestions to reduce complexity.

## Features

- **Multi-language**: Java, Groovy, Kotlin, JavaScript/TypeScript (including Vue SFC)
- **Cross-file analysis**: Follows call chains across files and languages to detect hidden O(n^2) patterns
- **Evidence chains**: Every finding explains *why* the pattern is problematic with a traceable call chain
- **SARIF output**: Native support for GitHub Code Scanning and VS Code integration
- **Configurable**: `.algorilla.yml` for project-specific rules, type hints, and exclusions
- **Deterministic**: No AI/ML — pure pattern matching on AST level, fully reproducible

## Quick Start

```bash
# Analyze current directory
java -jar algorilla.jar

# Analyze specific path with SARIF output
java -jar algorilla.jar --format sarif --output report.sarif src/

# Only errors, exclude test code
java -jar algorilla.jar --severity error --exclude "**/test/**" src/
```

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

## Building

```bash
git clone https://github.com/tvinke/algorilla.git
cd algorilla
./gradlew build
./gradlew shadowJar
java -jar cli/build/libs/algorilla.jar --help
```

## Author

Ted Vinke

## License

Apache License 2.0
