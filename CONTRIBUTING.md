# Contributing to algorilla

Thanks for considering contributing! Here's how to get started.

## Development setup

### Prerequisites

- JDK 21 (for building; the output targets Java 11)
- Gradle 8.x (wrapper included)

### Build

```bash
./gradlew build
```

### Run tests

```bash
./gradlew test
```

### Run against a project

```bash
./gradlew :cli:run --args="/path/to/your/project"
```

## Project structure

```
core/           Core IR model, analysis engine, rules framework, registry
lang-java/      Java parser (ANTLR)
lang-groovy/    Groovy parser (extends Java ANTLR grammar)
lang-kotlin/    Kotlin parser (text-based)
lang-javascript/ JavaScript/TypeScript parser (text-based)
reporting/      Console, SARIF, JSON reporters
cli/            Command-line interface (picocli)
build-logic/    Gradle convention plugins
```

## Adding a new rule

1. Create a new class in `core/src/main/kotlin/.../rules/builtin/` implementing `Rule`
2. Register it in `cli/src/main/kotlin/.../AlgorillaCommand.kt`
3. Add test fixtures in `lang-java/src/test/resources/fixtures/<rule-id>/positive/` and `negative/`
4. Add test cases in `RulesIntegrationTest.kt`

### Rule checklist

- [ ] Implements `Rule` interface with unique `id`
- [ ] Has positive and negative test fixtures
- [ ] Produces evidence chain for traceability
- [ ] Includes complexity before/after in findings
- [ ] Includes actionable suggestion

## Adding a new language

1. Create a new module `lang-<name>`
2. Implement `LanguageParser` interface
3. Register the parser in `AlgorillaCommand`
4. Add language to `Language` enum in core

## Extending the Collection Semantics Registry

Add methods to the YAML files in `core/src/main/resources/semantics/`. See existing files for the format. Available semantic categories: `iteration`, `lookup`, `sort`, `access`, `quadratic`, `blocking`, `serialization`, `repository-call`, `copy-on-modify`.

## Pull requests

- One feature or fix per PR
- Include tests for new rules
- Run `./gradlew check` before submitting (runs tests + ktlint + detekt)
- Keep PRs focused and reviewable

## Code style

The project uses ktlint and detekt. Run `./gradlew ktlintFormat` to auto-fix formatting.
