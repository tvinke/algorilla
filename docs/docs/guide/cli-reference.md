# CLI Reference

```
algorilla [OPTIONS] [PATHS...]
algorilla --input <path>[,<path>...] [OPTIONS]
```

## Input

Algorilla requires at least one input path. You can specify paths as positional arguments or with `--input`/`-i`:

```bash
# Positional (shorthand)
algorilla /path/to/project

# Named option
algorilla --input /path/to/project

# Multiple paths
algorilla src/main/java src/main/kotlin
algorilla -i src/main/java,src/main/kotlin
```

Running `algorilla` with no arguments prints an error — there is no implicit "scan current directory" default. This prevents accidental full-disk scans.

| Argument | Description |
|----------|-------------|
| `PATHS` | Files or directories to analyze (positional). |
| `-i`, `--input` | Files or directories to analyze (named, comma-separated). |

When pointing at a project root, Algorilla auto-detects source directories based on the build system (Gradle, Maven, JS/TS).

## Project Root Detection

Algorilla walks up from the given path to find the project root by looking for build-system markers (`build.gradle.kts`, `pom.xml`, `package.json`, etc.). This determines:

- **Where to scan**: source directories are resolved from project conventions (e.g. `src/main/java` per Gradle module).
- **Where `.algorilla/` lives**: the cache directory is always placed in the project root, not in the scan target.
- **Where to look for config**: `.algorilla.yml` is loaded from the project root.

If no build system is detected, the given path is used as-is and a notice is printed.

## Options

| Option | Description | Default |
|--------|-------------|---------|
| `-i`, `--input` | Files or directories to analyze (alternative to positional args) | |
| `-f`, `--format` | Output format: `console`, `sarif`, `json` | `console` |
| `-o`, `--output` | Write report to file instead of stdout | stdout |
| `-v`, `--verbose` | Show detailed analysis progress (DEBUG logging) | off |
| `--severity` | Minimum severity to report: `info`, `warning`, `error` | `warning` |
| `--confidence` | Minimum confidence to report: `low`, `medium`, `high`. HIGH shows only findings Algorilla is most sure about. | `high` |
| `--fail-on` | Minimum severity that triggers exit code 1: `info`, `warning`, `error` | `warning` |
| `--rule` | Only run specific rule(s), comma-separated or repeated | all rules |
| `--list-rules` | List all available rules and exit | |
| `--exclude` | Glob patterns to exclude from scanning | none |
| `-c`, `--config` | Path to `.algorilla.yml` config file | auto-detect |
| `--no-cache` | Disable incremental analysis caching | caching on |
| `--baseline` | Baseline file to compare against | none |
| `--save-baseline` | Save current findings to a baseline file | none |
| `--accept` | Accept findings by fingerprint hash, adding them to `.algorilla/ignore-list.json` | |
| `--include-tests` | Include test source files in analysis | excluded |
| `-l`, `--language` | Only analyze specified language(s), comma-separated or repeated | all |
| `--color` | Color output: `auto`, `always`, `never` | `auto` |
| `-h`, `--help` | Show help message | |
| `-V`, `--version` | Print version information | |

## Examples

```bash
# Scan current directory (auto-detects project structure)
algorilla .

# Scan specific project
algorilla /path/to/project

# Using named input
algorilla --input /path/to/project

# Targeted scan of a single module
algorilla /path/to/project/api/src/main/java

# SARIF output for CI
algorilla --format sarif -o results.sarif .

# Only errors, exclude generated code
algorilla --severity error --exclude "**/generated/**" .

# Save baseline, then only report new findings
algorilla --save-baseline baseline.json .
algorilla --baseline baseline.json .

# Force full re-analysis
algorilla --no-cache .

# List all available rules
algorilla --list-rules

# Run only specific rules
algorilla --rule nested-lookup,sort-for-last .

# Fail CI only on errors (warnings are advisory)
algorilla --fail-on error --format sarif -o results.sarif .

# Accept a reviewed finding
algorilla --accept a1b2c3d4e5f6g7h8 .

# Scan only Java and Kotlin files
algorilla --language java,kotlin .
```
