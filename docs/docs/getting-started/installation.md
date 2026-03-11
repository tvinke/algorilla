# Installation

## npm (recommended)

The quickest way to get started. No Java installation needed — a JRE is bundled automatically if Java isn't available on your system.

```bash
# Run directly
npx algorilla .

# Or install globally
npm install -g algorilla
algorilla .
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

The plugin auto-detects source directories from your project's source sets. Configure it in the `algorilla` block:

```kotlin
algorilla {
    minSeverity.set("warning")
    failOn.set("error")
    format.set("sarif")
    outputFile.set(layout.buildDirectory.file("reports/algorilla.sarif"))
}
```

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
