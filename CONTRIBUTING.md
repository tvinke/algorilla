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
core/             Core IR model, analysis engine, rules framework, ParserRegistry
lang-java/        Java parser (ANTLR)
lang-groovy/      Groovy parser (Java ANTLR grammar with preprocessing)
lang-kotlin/      Kotlin parser (tree-sitter + ANTLR fallback)
lang-javascript/  JavaScript/TypeScript parser (tree-sitter + regex fallback)
lang-template/    Starter template for adding new languages (not in build)
reporting/        Console, SARIF, JSON reporters
cli/              Command-line interface (picocli)
gradle-plugin/    Gradle plugin
build-logic/      Gradle convention plugins
docs/             MkDocs documentation site
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

A starter template is included in the repo — copy `lang-template/` and follow the TODOs. The full guide is at [Adding Languages](docs/docs/developer/adding-languages.md), but in short:

1. Copy `lang-template/` to `lang-<name>/`, add it to `settings.gradle.kts`
2. Add your language to the `Language` enum in core
3. Implement `LanguageParser` (the template has the error handling contract and self-registration pattern)
4. Add `YourLanguageParser.Companion` to `AlgorillaCommand.kt` and `AlgorillaTask.kt` (triggers class loading)
5. Create a semantics YAML under `core/src/main/resources/semantics/`
6. Add test fixtures and parser tests

See the [Adding Languages](docs/docs/developer/adding-languages.md) developer guide for the complete walkthrough with IR mapping table and examples.

## Adding a framework overlay

This is the easiest contribution path — no Kotlin code changes needed:

1. Create a YAML file under `core/src/main/resources/semantics/frameworks/` (copy an existing one as template)
2. Add a `language:` key at the top (e.g. `language: java`)
3. Add the filename to `core/src/main/resources/semantics/frameworks-index.txt`
4. Run `./gradlew build` to verify

The YAML defines method classifications (expensive calls, collection operations, etc.) for a specific framework. Once added, all existing rules automatically detect those framework methods. See the existing framework files (spring.yml, guava.yml, react.yml) for the format.

## Extending the Collection Semantics Registry

Add methods to the YAML files in `core/src/main/resources/semantics/`. See existing files for the format. Available semantic categories: `iteration`, `lookup`, `sort`, `access`, `quadratic`, `blocking`, `serialization`, `repository-call`, `copy-on-modify`.

## Pull requests

- One feature or fix per PR
- Include tests for new rules
- Run `./gradlew build` before submitting (compiles + tests + ktlint + detekt)
- Keep PRs focused and reviewable

## Code style

The project uses ktlint and detekt. Run `./gradlew ktlintFormat` to auto-fix formatting issues.
