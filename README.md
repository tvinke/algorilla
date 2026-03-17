# Algorilla

[![CI](https://img.shields.io/github/actions/workflow/status/tvinke/algorilla/ci.yml?branch=main&logo=github&label=CI)](https://github.com/tvinke/algorilla/actions/workflows/ci.yml)
[![Docs](https://img.shields.io/badge/docs-algorilla-blue?logo=materialformkdocs)](https://tvinke.github.io/algorilla/)
[![License](https://img.shields.io/github/license/tvinke/algorilla)](https://github.com/tvinke/algorilla/blob/main/LICENSE)
[![Languages](https://img.shields.io/badge/Java%20%7C%20Kotlin%20%7C%20Groovy%20%7C%20JS%2FTS-supported-green)]()

**Find the hidden O(n²) in your codebase before your users do.**

Algorilla is a static analysis tool that detects algorithmic complexity anti-patterns — the kind of performance bugs that pass code review, work fine with test data, and then crawl under real load.

## The problem

This code looks fine. It passes every code review:

```java
for (Order order : orders) {
    if (priorityIds.contains(order.getId())) {
        ship(order);
    }
}
```

But `List.contains()` is O(n). Inside that loop, the real cost is **O(orders × priorityIds)**. With 10,000 orders and 8,000 priority IDs, that's 80 million comparisons.

## What algorilla finds

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × priorityIds) → O(orders + priorityIds)
    com.example.shop.service.OrderService:3

      Linear contains on 'priorityIds' inside for-each loop
      → Build a HashSet/Map from 'priorityIds' before the loop
      ↗ https://tvinke.github.io/algorilla/rules/nested-lookup

          2 │ for (Order order : orders) {
          3 │     if (priorityIds.contains(order.getId())) {
          4 │         ship(order);

      ⎿  for-each loop over orders OrderService.java:2 O(orders)
        ⎿  contains on 'priorityIds' OrderService.java:3 O(priorityIds) ← bottleneck
```

Every finding shows the complexity before and after, a concrete suggestion, an evidence chain tracing the execution path, and a link to the rule docs. The fix usually takes 30 seconds.

## Quick start

```bash
npx algorilla .
```

That's it. Scans the current project, auto-detects the build system, and reports findings. Bundles a JRE if Java isn't on your PATH.

Too many findings? Start small:

```bash
npx algorilla --limit 5 .
```

## Install

**npm** (recommended):
```bash
npm install -g algorilla
```

**Gradle plugin**:
```kotlin
plugins {
    id("io.github.tvinke.algorilla") version "0.3.0"
}
```
```bash
./gradlew algorilla
```

**GitHub Action** (uploads SARIF to Code Scanning automatically):
```yaml
- uses: tvinke/algorilla@v0.3.0
  with:
    paths: '.'
```

**Docker** / **JAR**: see [installation docs](https://tvinke.github.io/algorilla/getting-started/installation/).

## What it detects

29 rules across 6 categories. A few highlights:

- **Nested lookups** — `contains()`/`filter()` inside a loop turning O(n) into O(n×m)
- **N+1 queries** — `findById()` or repository calls inside loops
- **Sort abuse** — sorting an entire collection just to pick the first/last element
- **IO in loops** — HTTP, DB, or file calls per iteration instead of batching
- **Hidden nested loops** — method calls that hide an inner loop behind an innocuous name
- **Redundant expensive calls** — same heavy computation repeated with the same arguments

Full rule reference with examples: [all rules](https://tvinke.github.io/algorilla/rules/)

## How it works

Pure AST pattern matching — no AI, no ML, fully deterministic. Algorilla parses your code into an intermediate representation, runs rules against it, and follows call chains across files. Results are reproducible: same code, same findings, every time.

**Languages**: Java (GA), Kotlin (Alpha), Groovy (Beta), JavaScript/TypeScript (Beta)
**Output**: Console (default), SARIF for GitHub Code Scanning, JSON for tooling
**Speed**: Incremental caching — only re-analyzes changed files

## Confidence levels

Not all findings are equally certain. Each one has a confidence tier:

- **HIGH** — structurally proven, very few false positives
- **MEDIUM** — likely correct, may need context
- **LOW** — heuristic-based, worth checking

Default output shows HIGH confidence only. Widen with `--confidence medium` or `--confidence low` as you triage.

## Stability

Pre-1.0. Breaking changes documented in the [CHANGELOG](CHANGELOG.md). CLI flags, rule IDs, JSON output, and exit codes are treated as stable — we avoid breaking them, and when we must, we call it out. See [Stability & Compatibility](https://tvinke.github.io/algorilla/stability/) for the full policy.

## Docs

- [Quick start](https://tvinke.github.io/algorilla/getting-started/quickstart/)
- [Understanding output](https://tvinke.github.io/algorilla/guide/understanding-output/)
- [CI/CD integration](https://tvinke.github.io/algorilla/guide/ci-integration/)
- [All rules](https://tvinke.github.io/algorilla/rules/)
- [Configuration](https://tvinke.github.io/algorilla/getting-started/configuration/)

## Building from source

```bash
./gradlew build        # compile + test + detekt + ktlint
./gradlew shadowJar    # create executable JAR
```

Requires JDK 21. See [developer docs](https://tvinke.github.io/algorilla/developer/building/).

## License

Apache License 2.0
