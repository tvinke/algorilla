# Installation

## npm (recommended)

The quickest way to get started. No Java installation needed — a JRE is bundled automatically if Java isn't available on your system.

```bash
# Run directly on the current project
npx algorilla .

# Or install globally
npm install -g algorilla
algorilla .                        # current directory
algorilla /path/to/your/project    # or point at any project
```

Requires Node.js 16+.

## Gradle Plugin

For Gradle projects, add the plugin to your build:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.tvinke.algorilla") version "0.2.0"
}
```

Then run:

```bash
./gradlew algorilla
```

The plugin auto-detects source directories from your project's Java/Kotlin/Groovy source sets. Configure it in the `algorilla` block:

```kotlin
algorilla {
    minSeverity.set("warning")   // minimum severity to report
    failOn.set("error")          // fail the build at this severity or above
    format.set("sarif")          // output format: json (default), sarif, console
    outputFile.set(layout.buildDirectory.file("reports/algorilla.sarif"))
}
```

All properties are optional — the defaults work out of the box. The report is written to `build/reports/algorilla.json` by default.

For the full list of options (rule filters, exclude patterns, baseline, test inclusion), see the [Gradle plugin configuration](#gradle-plugin-configuration) reference below.

## GitHub Action

Add algorilla to your CI pipeline:

```yaml
- uses: tvinke/algorilla@v0.2.0
  with:
    paths: '.'
    severity: 'warning'
```

SARIF results are automatically uploaded to GitHub Code Scanning, showing findings as annotations on PR diffs. See [CI/CD integration](../guide/ci-integration.md) for full examples.

## Docker

```bash
docker run --rm -v "$(pwd):/src" ghcr.io/tvinke/algorilla /src
```

Useful for environments where you don't want to install Java or Node.js.

## Download JAR

Download the latest `algorilla.jar` from the [releases page](https://github.com/tvinke/algorilla/releases) and run it directly:

```bash
java -jar algorilla.jar .
```

Requires Java 11+.

## Build from Source

```bash
git clone https://github.com/tvinke/algorilla.git
cd algorilla
./gradlew shadowJar
```

The fat JAR will be at `cli/build/libs/algorilla-<version>.jar`. Requires JDK 21.

## Verify Installation

```bash
algorilla --version
```

```
algorilla 0.2.0
```

## Gradle Plugin Configuration

Full reference for the `algorilla { }` extension block.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `minSeverity` | `String` | `"warning"` | Minimum severity to include in the report: `info`, `warning`, `error` |
| `failOn` | `String` | `"info"` | Minimum severity that triggers a build failure. Any finding at or above this level fails the task |
| `format` | `String` | `"json"` | Output format: `json`, `sarif`, `console` |
| `outputFile` | `File` | `build/reports/algorilla.json` | Where to write the report. Extension changes automatically with format |
| `rules` | `List<String>` | all | Only run these rule IDs. Empty list = all rules |
| `excludePatterns` | `List<String>` | none | Glob patterns for files to skip (e.g., `**/generated/**`) |
| `includeTests` | `Boolean` | `false` | Whether to scan test source sets |
| `baseline` | `File` | none | Path to a baseline file. Findings present in the baseline are filtered out |
| `limit` | `Int` | `0` | Maximum number of findings to display in console output. 0 = all |

These are the same concepts as the CLI flags (`--severity`, `--fail-on`, `--format`, etc.) and the [GitHub Action inputs](../guide/ci-integration.md#action-inputs). The naming differs slightly to match Gradle conventions.
