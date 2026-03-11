# Contributing to Algorilla

Thanks for considering contributing! Whether it's reporting a false positive, suggesting a rule improvement, or submitting code — all contributions are welcome.

## Reporting issues

### False positives

If algorilla flags code that isn't actually a problem, please open an issue using the **False Positive** template. Include:

- The **rule ID** (e.g. `nested-lookup`)
- A **minimal code snippet** that triggers the finding (or a link to a public repo + file/line)
- **Why you think it's a false positive** — what makes this case different from the pattern the rule targets

Good false positive reports help us make algorilla more precise without losing real detections. Even edge cases are valuable.

### False negatives

If algorilla misses something it should catch, open an issue using the **False Negative** template. Include:

- The **rule ID** you expected to trigger (or describe the pattern if no rule exists)
- A **code snippet** showing the pattern
- **What the expected finding would look like**

### Scan feedback

Ran algorilla on your codebase and have observations? We'd love to hear about it — open an issue using the **Scan Feedback** template. Useful things to share:

- What worked well, what was noisy
- Rules that are too strict or too lenient for your codebase
- Patterns you saw that algorilla doesn't cover yet

You don't need to have a fix in mind. Real-world usage feedback drives rule quality.

### Bugs and feature requests

Use the **Bug Report** or **Feature Request** templates.

## Development setup

### Prerequisites

- JDK 21 (for building; the output targets Java 11)
- Gradle 8.x (wrapper included)

### Build

```bash
./gradlew build
```

This runs compilation, tests, detekt, and ktlint. Always run the full build before submitting a PR — running only `test` skips static analysis.

### Run against a project

```bash
./gradlew :cli:run --args="/path/to/your/project"
```

## Project structure

```
core/             Core IR model, analysis engine, rules framework, registry
lang-java/        Java parser (ANTLR)
lang-groovy/      Groovy parser (extends Java ANTLR grammar)
lang-kotlin/      Kotlin parser (text-based)
lang-javascript/  JavaScript/TypeScript parser (text-based)
reporting/        Console, SARIF, JSON reporters
cli/              Command-line interface (picocli)
gradle-plugin/    Gradle plugin
build-logic/      Gradle convention plugins
```

## Adding a new rule

1. Create a new class in `core/src/main/kotlin/.../rules/builtin/` implementing `Rule`
2. Register it in `BuiltinRules.kt` (same package)
3. Add test fixtures in the relevant `lang-*/src/test/resources/fixtures/<rule-id>/` directories
4. Add a dedicated test class in `core/src/test/kotlin/.../rules/builtin/`
5. Add a doc page at `docs/docs/rules/<rule-id>.md` and update `docs/docs/rules/index.md`

### Rule checklist

- [ ] Implements `Rule` interface with unique `id`
- [ ] Registered in `BuiltinRules.all()`
- [ ] Has positive and negative test fixtures
- [ ] Produces evidence chain for traceability
- [ ] Includes complexity before/after in findings
- [ ] Includes actionable suggestion
- [ ] Has a doc page with examples

## Adding a new language

1. Create a new module `lang-<name>`
2. Implement `LanguageParser` interface
3. Register the parser in `AlgorillaCommand.kt` and `AlgorillaTask.kt`
4. Add language to `Language` enum in core

## Extending the Collection Semantics Registry

Add methods to the YAML files in `core/src/main/resources/semantics/`. See existing files for the format. Available semantic categories: `iteration`, `lookup`, `sort`, `access`, `quadratic`, `blocking`, `serialization`, `repository-call`, `copy-on-modify`.

## Pull requests

- One feature or fix per PR
- Include tests for new rules
- Run `./gradlew build` before submitting (compiles + tests + ktlint + detekt)
- Keep PRs focused and reviewable

## Code style

The project uses ktlint and detekt. Run `./gradlew ktlintFormat` to auto-fix formatting issues.
