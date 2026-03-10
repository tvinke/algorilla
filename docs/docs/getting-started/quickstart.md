# Quick Start

## Scan a Project

Point Algorilla at your project root:

```bash
java -jar algorilla.jar /path/to/your/project
```

!!! tip "Zero config"
    Algorilla automatically detects the build system (Gradle, Maven, or JS/TS), resolves where source code lives, and excludes test code and build output. There is no need to point at `src/main` — just pass the project root.

For example, for a Gradle project:

```bash
java -jar algorilla.jar .
```

This detects all modules from `settings.gradle.kts` and scans their `src/main/{java,kotlin,groovy}` directories.

## Scan Specific Files or Directories

You can also point at a specific file or subdirectory for a targeted scan:

```bash
# Single file
java -jar algorilla.jar src/main/java/com/example/OrderService.java

# Specific subdirectory
java -jar algorilla.jar src/main/groovy
```

When targeting a subdirectory, Algorilla still resolves the project root upwards and places its `.algorilla/` cache there.

## Supported Build Systems

| Build system | Detection | Source roots scanned |
|---|---|---|
| Gradle | `build.gradle(.kts)` or `settings.gradle(.kts)` | `src/main/{java,kotlin,groovy}` per module |
| Maven | `pom.xml` | `src/main/java` per module |
| JS/TS | `package.json` | Project root (with `node_modules/` excluded) |
| None detected | — | The given path as-is |

## Output Formats

Console output (default):

```bash
java -jar algorilla.jar .
```

SARIF for CI/CD integration:

```bash
java -jar algorilla.jar --format sarif --output results.sarif .
```

JSON for programmatic processing:

```bash
java -jar algorilla.jar --format json --output results.json .
```

## Common Options

```bash
# Show only errors (skip warnings)
java -jar algorilla.jar --severity error .

# Exclude specific directories
java -jar algorilla.jar --exclude "**/generated/**" .

# Include test code in analysis
java -jar algorilla.jar --include-tests .

# Verbose output for debugging
java -jar algorilla.jar -v .
```

## Exit codes

| Code | Meaning |
|------|---------|
| 0 | No findings |
| 1 | Findings detected |
| 2 | Analysis error |

!!! info "CI-friendly"
    Exit code 1 on findings makes algorilla work as a quality gate out of the box — pipe it into your CI pipeline and it will fail the build when issues are found. See [CI/CD integration](../guide/ci-integration.md) for setup guides.
