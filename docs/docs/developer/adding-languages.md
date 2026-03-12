# Adding Languages

## Overview

Each language lives in its own Gradle module (`lang-<name>/`). The module contains one class implementing `LanguageParser`, which converts source files into the unified IR tree. The engine and CLI discover parsers at runtime through `ParserRegistry` — no hardcoded list to update.

## 1. Create the module

Copy `lang-template/` (in the repo root) as a starting point. It contains a parser skeleton with TODOs, a tree-sitter visitor template, test scaffolding, and fixture directory structure.

Add the new module to `settings.gradle.kts`. The template is not included in the build by default — see the comment at the bottom of that file.

## 2. Add the Language enum entry

Add your language to `core/src/main/kotlin/.../model/Language.kt`. Each entry has a `displayName` and a set of file `extensions` — see the existing entries for the format.

## 3. Implement LanguageParser

The template's `TemplateParser.kt` has the full pattern with inline comments. The key contracts:

- Implement `LanguageParser` with `language`, `canParse(filePath)`, and `parse(filePath)`
- `parse()` must **never throw** — catch all exceptions, log at WARNING level, return an empty `FileRoot`
- Self-register via `companion object init { ParserRegistry.register(YourParser()) }`

For working examples beyond the template, see `KotlinLanguageParser.kt` (tree-sitter + ANTLR fallback) or `JavaLanguageParser.kt` (pure ANTLR).

## 4. Trigger class loading from the CLI

In `AlgorillaCommand.kt`, add `YourParser.Companion` to the `registerAllParsers()` function. Do the same in `AlgorillaTask.kt` in the Gradle plugin module.

!!! warning "This step is easy to forget and hard to debug"

    If you skip it, your parser compiles, tests pass, but files are silently skipped at runtime — no error, no warning. The `ParserRegistrationTest` in the CLI module will catch this: if you add a parser but don't wire it here, that test fails.

## 5. IR mapping

Map language constructs to the unified IR model:

| Language construct | IR node |
|--------------------|---------|
| for / while / do-while | `LoopNode` with `LoopKind.FOR` / `WHILE` |
| forEach, stream iteration | `LoopNode` with `LoopKind.FOR_EACH` |
| List.get, Map.get, contains | `LookupCall` |
| Collections.sort, .sorted() | `SortCall` |
| Function / method definition | `FunctionDecl` |
| Method / function call | `FunctionCall` |
| new Foo() | `ObjectCreation` |
| Variable declaration | `VariableDecl` |

Set `executionContext` on nodes inside callbacks or sort comparators to `ExecutionContext.CALLBACK` or `SORT_COMPARATOR` respectively.

The template's `TemplateTreeSitterVisitor.kt` has comments per IR node type explaining which rules depend on it.

## 6. Add semantics YAML

Create `core/src/main/resources/semantics/<language>.yml` and list the methods relevant for rule detection (expensive calls, cheap calls, stream operations, etc.). See the existing YAML files for the 17-section structure. Then add the filename to `BASE_SEMANTICS_RESOURCES` in `AnalysisCache.kt`.

For framework overlays, add a file under `semantics/frameworks/` and append its name to `semantics/frameworks-index.txt`. Include a `language:` key at the top of the YAML so the registry can load it without any code changes.

## 7. Testing

- Write unit tests for the parser itself using real source snippets as fixtures
- Add rule-specific fixtures under `src/test/resources/fixtures/<rule-id>/positive/` and `negative/`
- Run the full pipeline (parse → IR → rules → findings) in at least one integration test per rule
- Run `./gradlew :lang-<name>:build` before opening a PR

See the test classes in `lang-kotlin/src/test/kotlin/` or `lang-groovy/src/test/kotlin/` for the established pattern.

## Known gotchas

### Companion object registration is silent-fail

If you implement a parser with self-registration but forget to add `YourParser.Companion` to `registerAllParsers()` in `AlgorillaCommand.kt` (and `AlgorillaTask.kt`), your parser will never load. There's no error — files are simply skipped. If your parser tests pass but scanning a real project finds nothing, this is the first thing to check.

### Groovy fixture syntax

The Groovy parser preprocesses source files into Java-compatible syntax before passing them to the Java ANTLR grammar. This means test fixtures for Groovy must use Java-style syntax:

- Use explicit types, not `def` (write `String name` not `def name`)
- Use Java lambda syntax `item -> { ... }`, not Groovy closure syntax `{ item -> ... }`
- Avoid Groovy-specific operators like `?.`, `*.`, `=~`
- See existing Groovy fixtures in `lang-groovy/src/test/resources/fixtures/` for working examples

### JavaLanguageParser naming

All parsers follow the `{Lang}LanguageParser` naming pattern — except the Java parser stays `JavaLanguageParser` (not `JavaParser`) to avoid collision with the ANTLR-generated `JavaParser` class used extensively throughout the codebase. If your language also uses an ANTLR grammar with a generated `*Parser` class, use the `*LanguageParser` form to avoid ambiguity.
