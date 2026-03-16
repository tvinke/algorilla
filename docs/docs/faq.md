# FAQ

## What does Algorilla actually do?

Algorilla is a command-line static analysis tool that scans your source code for algorithmic complexity anti-patterns — the kind of performance bugs that pass code review, work fine in tests, and slow down production. It reads your source files (without executing them), builds a model of loops, method calls, and data flow, and reports patterns where a simple refactoring can dramatically reduce the cost.

For a deeper look, see [how detection works](concepts/how-detection-works.md).

## Does it need to compile my project?

No. Algorilla works directly on source files — no build step, no classpath, no compiled classes. You can run it on a project that doesn't even compile yet. The tradeoff is that some type information isn't available (see next question).

## Why doesn't it know my variable's type?

Algorilla resolves types from what it can see in source code: variable declarations, constructor calls, parameter types, and type annotations. It does **not** scan your classpath, inspect bytecode, or resolve types from compiled dependencies.

If a method returns `Collection<String>` but the runtime type is `HashSet`, Algorilla can't see that — it only sees `Collection`. In these cases, you can provide [type hints](getting-started/configuration.md) in `.algorilla.yml` to tell it what a variable actually is.

For the full explanation of how type resolution works and its limits, see [detection mechanisms](concepts/how-detection-works.md#detection-mechanisms).

## What does "LOW confidence" mean?

It means Algorilla thinks it found something, but isn't sure. LOW confidence findings typically rely on [name-based heuristics](concepts/how-detection-works.md#name-based-heuristics-low-confidence) — guessing from method and variable names rather than proving from types. For example, `order.getLineItems()` _looks like_ a lazy-loaded collection getter, but it might be a preloaded field.

LOW confidence findings are hidden by default (the default is `--confidence medium`). Use `--confidence low` to see them.

Confidence is separate from severity: a LOW confidence finding can still be a WARNING (high-impact if real), and a HIGH confidence finding can be INFO (definitely real but maybe not worth fixing).

## What's the difference between severity and confidence?

**Severity** = how bad is the problem if it's real? WARNING means likely performance impact at scale. INFO means worth knowing but may not matter.

**Confidence** = how sure is the tool? HIGH means structurally proven. MEDIUM means likely correct but depends on context. LOW means heuristic guess based on naming conventions.

A finding can be any combination: high-severity + low-confidence ("this would be bad, but we're not sure it's happening"), or low-severity + high-confidence ("this is definitely happening, but it's minor").

See [severity levels](guide/understanding-output.md#severity-levels) and [confidence levels](guide/understanding-output.md#confidence-levels) for details.

## What does O(n²) mean?

It's [Big-O notation](concepts/big-o-primer.md) — a shorthand for "how does cost grow with input size." O(n) means cost grows linearly (double the input, double the work). O(n²) means it grows with the square (double the input, quadruple the work).

The [Big-O primer](concepts/big-o-primer.md) explains it fully in about 5 minutes. The [hidden O(n²) problem](hidden-complexity.md) shows how it hides in real code.

## Which languages are supported?

- **[Java](languages/java.md)** (GA) — most mature, full type resolution, lowest false-positive rate
- **[Kotlin](languages/kotlin.md)** (Alpha) — early support, higher false-positive rate
- **[Groovy](languages/groovy.md)** (Beta) — usable with caveats, dynamic typing limits accuracy
- **[JavaScript/TypeScript](languages/javascript.md)** (Beta) — name-based heuristics, `.vue` SFC support

Most rules apply to all languages — a few target JVM-specific APIs and don't apply to JavaScript/TypeScript. See the [language support overview](languages/index.md) for the full coverage matrix.

## How is this different from a profiler?

A profiler measures what's slow in running code. Algorilla looks at source code for patterns that tend to become slow at scale — without needing to run anything or set up production-like load.

They're complementary: Algorilla flags structural patterns (like a `List.contains()` inside a loop — O(n²) by design), while a profiler catches runtime issues (slow queries, memory pressure, GC pauses).

## Can I use it in CI?

Yes — Algorilla is designed for CI. It produces SARIF output for GitHub Code Scanning, has configurable exit codes for quality gates, and supports baseline workflows so you only see _new_ findings. See [CI/CD integration](guide/ci-integration.md).

## How do I suppress a finding?

Two ways:

1. **Inline:** add `// algorilla:ignore` (or `// algorilla:ignore rule-id`) on the line before the finding
2. **Baseline:** run `algorilla --accept <hash>` to accept a specific finding by its fingerprint hash

See [suppression](guide/suppression.md) for details.

## How does Algorilla compare to SonarQube, PMD, or ESLint?

Different focus. Algorilla only cares about algorithmic complexity — the patterns that make code slow at scale. The others cover a much broader range of issues (bugs, security, code smells, style).

| | Algorilla | SonarQube | PMD / SpotBugs | ESLint |
|---|---|---|---|---|
| **Focus** | Algorithmic complexity | Bugs, security, code smells | Bugs, style, best practices | JS/TS style, bugs |
| **Detects O(n²) patterns** | Primary focus | Limited (some perf rules) | No | No |
| **Evidence chains** | Yes | No | No | No |
| **Cross-file analysis** | Yes | Yes | Limited | No |
| **Languages** | Java, Kotlin, Groovy, JS/TS | 30+ | Java only | JS/TS only |

They're complementary — run Algorilla alongside your existing linter, not instead of it. SonarQube catches null dereferences and SQL injection; Algorilla catches the `List.contains()` inside a loop that SonarQube won't flag.

## What frameworks does it know about?

Algorilla ships with built-in knowledge of 15+ frameworks including Spring, JPA/Hibernate, Guava, Reactor, Jackson, Kotlin Coroutines, Ktor, Grails/GORM, Vue, React, Angular, RxJS, Lodash, and Node.js. This knowledge reduces false positives — the tool understands which framework methods are cheap, which are expensive, and which trigger I/O.

See [framework support](languages/frameworks.md) for the full list.
