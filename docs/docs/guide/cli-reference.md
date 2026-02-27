# CLI Reference

```
algorilla [OPTIONS] [PATHS...]
```

## Positional Arguments

| Argument | Description |
|----------|-------------|
| `PATHS` | Files or directories to analyze. Default: current directory. |

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
# Scan current directory
java -jar algorilla.jar

# Scan specific project
java -jar algorilla.jar /path/to/project

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
