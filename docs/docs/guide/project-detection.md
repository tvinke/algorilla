# Project Detection

Algorilla automatically detects your project's build system and resolves where source code lives. This means you can point it at a project root and it figures out the rest.

## How It Works

When you run `algorilla /path/to/project`, Algorilla:

1. **Finds the project root** by walking up from the given path, looking for build-system marker files (`build.gradle.kts`, `pom.xml`, `package.json`, etc.).
2. **Detects source directories** based on the build system's conventions.
3. **Places `.algorilla/`** (cache, baseline) in the project root — never in a subdirectory.

If no build system is detected, Algorilla scans the given path as-is.

## Supported Build Systems

### Gradle

Detected by: `build.gradle.kts`, `build.gradle`, `settings.gradle.kts`, or `settings.gradle`.

For single-module projects, Algorilla scans `src/main/{java,kotlin,groovy}` — whichever directories exist.

For multi-module projects, it parses `settings.gradle.kts` for `include(...)` statements and scans each module's `src/main/{java,kotlin,groovy}`.

```bash
# Scans all modules automatically
algorilla /path/to/gradle-project
```

### Maven

Detected by: `pom.xml`.

For single-module projects, Algorilla scans `src/main/java`.

For multi-module projects, it parses `<modules>` from the root POM and scans each module's `src/main/java`.

```bash
algorilla /path/to/maven-project
```

### JavaScript / TypeScript

Detected by: `package.json`.

Algorilla scans the entire project root. The `node_modules/`, `dist/`, and `build/` directories are excluded automatically.

```bash
algorilla /path/to/js-project
```

## Targeted Scans

You can bypass auto-detection by pointing at a specific file or subdirectory:

```bash
# Only scan the API module's Groovy sources
algorilla /path/to/project/api/src/main/groovy

# Scan a single file
algorilla src/main/java/com/example/OrderService.java
```

Even during a targeted scan, Algorilla still resolves the project root upwards so that `.algorilla/` is placed in the right location.

## Cache Directory

Algorilla stores its incremental analysis cache in `.algorilla/analysis-cache.json` at the project root. Add this to your `.gitignore`:

```gitignore
.algorilla/
```

## Test Code Exclusion

By default, Algorilla excludes test source directories:

- **Gradle**: `src/test/` (and per-module equivalents)
- **Maven**: `src/test/`
- **Fallback** (no build system): `src/test/`, `test/`, `__tests__/`, files matching `.test.` or `.spec.`

To include test code in the analysis:

```bash
algorilla --include-tests .
```

## Default Exclusions

These directories are always excluded from scanning, regardless of build system:

- `node_modules/`
- `build/`
- `target/`
- `dist/`
- `.gradle/`
- `.git/`
- `.idea/`
- `generated/`
- `.algorilla/`
