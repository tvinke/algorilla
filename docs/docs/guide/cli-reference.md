# CLI Reference

```
algorilla [OPTIONS] [PATHS...]
```

## Positional Arguments

| Argument | Description |
|----------|-------------|
| `PATHS` | Files or directories to analyze. Default: current directory. When pointing at a project root, Algorilla auto-detects source directories based on the build system (Gradle, Maven, JS/TS). |

## Project Root Detection

Algorilla walks up from the given path to find the project root by looking for build-system markers (`build.gradle.kts`, `pom.xml`, `package.json`, etc.). This determines:

- **Where to scan**: source directories are resolved from project conventions (e.g. `src/main/java` per Gradle module).
- **Where `.algorilla/` lives**: the cache directory is always placed in the project root, not in the scan target.
- **Where to look for config**: `.algorilla.yml` is loaded from the project root.

If no build system is detected, the given path is used as-is.

## Options

| Option | Description | Default |
|--------|-------------|---------|
| `-f`, `--format` | Output format: `console`, `sarif`, `json` | `console` |
| `-o`, `--output` | Write report to file instead of stdout | stdout |
| `-v`, `--verbose` | Show detailed analysis progress (DEBUG logging) | off |
| `--severity` | Minimum severity: `info`, `warning`, `error` | `warning` |
| `--exclude` | Glob patterns to exclude from scanning | none |
| `-c`, `--config` | Path to `.algorilla.yml` config file | auto-detect |
| `--no-cache` | Disable incremental analysis caching | caching on |
| `--baseline` | Baseline file to compare against | none |
| `--save-baseline` | Save current findings to a baseline file | none |
| `--include-tests` | Include test source files in analysis | excluded |
| `-h`, `--help` | Show help message | |
| `-V`, `--version` | Print version information | |

## Examples

```bash
# Scan current directory (auto-detects project structure)
java -jar algorilla.jar

# Scan specific project
java -jar algorilla.jar /path/to/project

# Targeted scan of a single module
java -jar algorilla.jar /path/to/project/api/src/main/java

# SARIF output for CI
java -jar algorilla.jar --format sarif -o results.sarif .

# Only errors, exclude generated code
java -jar algorilla.jar --severity error --exclude "**/generated/**" .

# Save baseline, then only report new findings
java -jar algorilla.jar --save-baseline baseline.json .
java -jar algorilla.jar --baseline baseline.json .

# Force full re-analysis
java -jar algorilla.jar --no-cache .
```
