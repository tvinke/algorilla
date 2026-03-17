# Quick Start

Algorilla is a command-line tool that scans your source code for algorithmic complexity anti-patterns. It supports [Java](../languages/java.md) (GA), [Kotlin](../languages/kotlin.md) (Alpha), [Groovy](../languages/groovy.md) (Beta), and [JavaScript/TypeScript](../languages/javascript.md) (Beta).

After [installing](installation.md), you run it from your terminal — point it at a project, and it reports what it finds.

## Scan a Project

Point algorilla at your project root:

```bash
algorilla /path/to/your/project
```

Or use the named `--input` option:

```bash
algorilla --input /path/to/your/project
```

!!! tip "Zero config"
    Algorilla automatically detects the build system (Gradle, Maven, or JS/TS), resolves where source code lives, and excludes test code and build output. There is no need to point at `src/main` — just pass the project root. Note: at least one input path is required — running `algorilla` with no arguments will show an error.

For a Gradle project in the current directory:

```bash
algorilla .
```

This detects all modules from `settings.gradle.kts` and scans their `src/main/{java,kotlin,groovy}` directories.

## Scan Specific Files or Directories

You can also point at a specific file or subdirectory for a targeted scan:

```bash
# Single file
algorilla src/main/java/com/example/OrderService.java

# Specific subdirectory
algorilla src/main/groovy
```

When targeting a subdirectory, Algorilla still resolves the project root upwards and places its `.algorilla/` cache there.

## Supported Build Systems

| Build system | Detection | Source roots scanned |
|---|---|---|
| Gradle | `build.gradle(.kts)` or `settings.gradle(.kts)` | `src/main/{java,kotlin,groovy}` per module |
| Maven | `pom.xml` | `src/main/java` per module |
| JS/TS | `package.json` | Project root (with `node_modules/` excluded) |
| None detected | — | The given path as-is |

## Example Output

When algorilla finds something, it prints findings grouped by file with a suggestion and a link to the rule docs:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × priorityIds) → O(orders + priorityIds)
    com.example.shop.service.OrderService:12

      Linear contains on 'priorityIds' inside for-each loop
      → Build a HashSet/Map from 'priorityIds' before the loop
      ↗ https://tvinke.github.io/algorilla/rules/nested-lookup

          11 │ for (Order order : orders) {
          12 │     if (priorityIds.contains(order.getId())) {
          13 │         ship(order);

      ⎿  for-each loop over orders OrderService.java:11 O(orders)
        ⎿  contains on 'priorityIds' OrderService.java:12 O(priorityIds) ← bottleneck
      # a1b2c3d4  ← accept with --accept a1b2c3d4

Scanned 42 files in 0.3s. Found 1 issue (1 warning) across 1 file.
Tip: --accept <hash> to mark reviewed, // algorilla:ignore to suppress in code
```

Each finding shows severity, rule name, complexity trade-off, a human-readable description, a concrete fix suggestion, and an evidence chain tracing the path to the bottleneck. The `# a1b2c3d4` at the end is the finding's **fingerprint hash** — a stable identifier you can use with `--accept <hash>` to mark a finding as reviewed. See [understanding output](../guide/understanding-output.md) for the full format reference.

## Output Formats

Console output (default):

```bash
algorilla .
```

SARIF for CI/CD integration:

```bash
algorilla --format sarif --output results.sarif .
```

JSON for programmatic processing:

```bash
algorilla --format json --output results.json .
```

## Common Options

```bash
# Show only errors (skip warnings)
algorilla --severity error .

# Exclude specific directories
algorilla --exclude "**/generated/**" .

# Include test code in analysis
algorilla --include-tests .

# Verbose output for debugging
algorilla -v .

# Cap output at 5 findings (great for first evaluation)
algorilla --limit 5 .
```

## Confidence Levels

By default, algorilla shows only **high-confidence** findings — patterns where it has strong evidence (type-confirmed targets, unambiguous IO methods, structural patterns). This keeps your first scan focused and trustworthy.

As you fix those, widen the net:

```bash
# See more findings that are likely real but may need judgment
algorilla --confidence medium .

# See everything, including speculative findings
algorilla --confidence low .
```

| Level | What you see | When to use |
|-------|-------------|-------------|
| `high` (default) | Type-confirmed and structural patterns | First scan, CI quality gates |
| `medium` | Findings that are likely real but lack full type evidence | After fixing high-confidence batch |
| `low` | Everything, including heuristic-based findings | Exploration, full audit |

The summary line tells you what's hidden: "5 high-confidence findings. 42 more at `--confidence medium`."

## Exit codes

| Code | Meaning |
|------|---------|
| 0 | No findings (or all findings below `--fail-on` threshold) |
| 1 | Findings at or above `--fail-on` severity (default: `warning`) |
| 2 | Analysis error |

!!! info "CI-friendly"
    Exit code 1 on findings makes algorilla work as a quality gate out of the box. By default, warnings and errors trigger a non-zero exit. Use `--fail-on error` for a more lenient gate. See [CI/CD integration](../guide/ci-integration.md) for setup guides.
