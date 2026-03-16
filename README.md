# Algorilla

[![CI](https://img.shields.io/github/actions/workflow/status/tvinke/algorilla/ci.yml?branch=main&logo=github&label=CI)](https://github.com/tvinke/algorilla/actions/workflows/ci.yml)
[![Docs](https://img.shields.io/badge/docs-algorilla-blue?logo=materialformkdocs)](https://tvinke.github.io/algorilla/)
[![License](https://img.shields.io/github/license/tvinke/algorilla)](https://github.com/tvinke/algorilla/blob/main/LICENSE)
[![Languages](https://img.shields.io/badge/Java%20%7C%20Kotlin%20%7C%20Groovy%20%7C%20JS%2FTS-supported-green)]()

**Find the hidden O(n^2) in your codebase before your users do.**

Algorilla is a static analysis tool that detects algorithmic complexity anti-patterns in Java, Groovy, Kotlin, and JavaScript/TypeScript code. It spots the kind of performance bugs that pass code review, work fine in tests, and then bring production to its knees when real data hits.

## The Problem

This code looks fine. It passes every code review. It works perfectly in your test suite with 10 items:

```java
void processOrders(List<Order> orders, List<String> priorityIds) {
    for (Order order : orders) {
        if (priorityIds.contains(order.getId())) {
            ship(order);
        }
    }
}
```

But `List.contains()` is O(n) — it scans the entire list on every call. Inside that loop, the real complexity is **O(n * m)**. With 10,000 orders and 8,000 priority IDs? That's **80 million comparisons**.

## What Algorilla finds

Run it on the code above:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × priorityIds) → O(orders + priorityIds)
    com.example.shop.service.OrderService:3

      Linear contains on 'priorityIds' inside for-each loop
      → Build a HashSet/Map from 'priorityIds' before the loop
      ↳ https://tvinke.github.io/algorilla/rules/nested-lookup

          2 │ for (Order order : orders) {
          3 │     if (priorityIds.contains(order.getId())) {
          4 │         ship(order);

      ⎿  for-each loop over orders OrderService.java:2 O(orders)
        ⎿  contains on 'priorityIds' OrderService.java:3 O(priorityIds) ← bottleneck
```

The fix takes 30 seconds — convert `priorityIds` to a `HashSet` before the loop. 80 million operations become 10,000.

## Installation

### npm (recommended)

```bash
npx algorilla .
# or install globally
npm install -g algorilla
algorilla .
```

Bundles a JRE automatically if Java isn't available.

### Gradle plugin

```kotlin
// build.gradle.kts
plugins {
    id("io.github.tvinke.algorilla") version "0.2.0"
}
```

```bash
./gradlew algorilla
```

### GitHub Action

```yaml
- uses: tvinke/algorilla@v0.2.0
  with:
    paths: '.'
```

Uploads SARIF to GitHub Code Scanning automatically.

### Docker

```bash
docker run --rm -v "$(pwd):/src" ghcr.io/tvinke/algorilla /src
```

### Download JAR

Download from [releases](https://github.com/tvinke/algorilla/releases) and run directly:

```bash
java -jar algorilla.jar .
```

Requires Java 11+.

## Features

- **Multi-language**: Java, Kotlin, Groovy, JavaScript/TypeScript (including `.vue` single-file components)
- **Cross-file analysis**: Follows call chains across files and languages
- **Evidence chains**: Every finding shows *exactly* why it's a problem
- **SARIF output**: Plugs into GitHub Code Scanning and VS Code
- **Incremental**: Caches results per file — re-analyzes only what changed
- **Baseline mode**: Suppress existing findings, catch only new ones
- **Configurable**: `.algorilla.yml` for rules, type hints, exclusions
- **Custom rules**: Extend with your own patterns via Kotlin Script DSL
- **Deterministic**: No AI/ML — pure AST pattern matching, fully reproducible

## Rules

29 built-in rules across 6 categories, each with a confidence tier (HIGH / MEDIUM / LOW) so you know which findings to act on first. See [all rules](docs/docs/rules/index.md) for details.

| Rule | What it catches | Impact |
|------|----------------|--------|
| `nested-lookup` | `contains()`/`filter()`/`find()` inside a loop | O(n*m) → O(n+m) |
| `repeated-linear-scan` | Same collection scanned 2+ times in one method | k * O(n) → O(n) |
| `sort-for-last` | `sort()` just to get `first()`/`last()` | O(n log n) → O(n) |
| `expensive-sort-comparator` | Linear search, date parsing, or heavy objects in sort comparator | O(n² log n) → O(n log n) |
| `filter-after-sort` | `sorted().filter()` — filtering after sorting wastes sort effort | O(n log n) → O(k log k) |
| `bulk-load-for-single-lookup` | `findAll()` + filter when a targeted query would do | O(n) → O(1) |
| `n-plus-one-query` | `findById()`/`countBy*` inside a loop | O(n * IO) → O(1 * IO) |
| `expensive-construction` | `new ObjectMapper()` inside a method body | ~1ms allocation per call |
| `repeated-regex-in-loop` | `Pattern.compile()` / `new RegExp()` inside loop | Recompilation per iteration |
| `regex-recompilation-in-loop` | `String.matches()`/`replaceAll()` inside loop | Hidden recompilation per iteration |
| `expensive-serialization-in-loop` | `writeValueAsString()`/`JSON.parse()` inside loop | O(n * serialize) |
| `sequential-async-join-in-loop` | `.join()`/`.get()` on futures in loop | Sequential I/O instead of parallel |
| `in-loop-collection-building` | `addAll()`/`concat()` inside loop | O(n*m) copying per iteration |
| `string-concat-in-loop` | String concatenation with `+=` inside loop | O(n²) copying |
| `quadratic-removal` | `remove()`/`removeFirst()` on ArrayList in loop | O(n²) shifting |
| `repeated-reflection-in-loop` | Reflection calls inside loop | O(n * reflection) |
| `hidden-nested-loop` | Method call inside loop hides an inner loop | O(n*m) hidden behind method call |
| `expensive-callback` | Expensive operations inside `filter`/`map`/`forEach` | O(n * expensive-op) |
| `redundant-expensive-call` | Same call with same args invoked multiple times | k * O(f) → O(f) |
| `uncached-getter` | `getXxx(id)` called repeatedly with same argument | Cache in local variable |
| `chained-getters` | Cascading getter chain: `a=get(x)` → `b=get(a)` → `c=get(b)` | Compound lookup cost |
| `parallel-pipeline-bottleneck` | Shared mutable state in `parallelStream().forEach()` | Thread-safety + serialization |

## Stability

Algorilla is pre-1.0. Breaking changes may occur between minor versions (0.x → 0.y). Pin your version in CI and check the [CHANGELOG](CHANGELOG.md) before upgrading.

CLI flags, config keys, rule IDs, JSON output fields, and exit codes are treated as stable — we avoid breaking them, and when we must, we document it clearly. See [Stability & Compatibility](docs/docs/stability.md) for the full policy.

## Documentation

- [Getting started](docs/docs/getting-started/quickstart.md)
- [CLI reference](docs/docs/guide/cli-reference.md)
- [CI/CD integration](docs/docs/guide/ci-integration.md)
- [All rules](docs/docs/rules/index.md)
- [Configuration](docs/docs/getting-started/configuration.md)

## Building from Source

Requires JDK 21 and Gradle 8.12+ (wrapper included).

```bash
./gradlew build        # compile + test + detekt + ktlint
./gradlew shadowJar    # create executable JAR
```

See [developer docs](docs/docs/developer/building.md) for details.

## Author

Ted Vinke

## License

Apache License 2.0
