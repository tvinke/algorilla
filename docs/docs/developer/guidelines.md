# Development Guidelines

Every change to Algorilla should include three things: working code, tests, and documentation. This page describes what is expected and when.

## Tests Are Required

Every pull request must include tests that cover the new or changed behavior.

| Change type | Expected tests |
|---|---|
| New rule | Unit test with positive and negative fixtures |
| New parser or language | Parser test with fixture files for each supported construct |
| Bug fix | Regression test that fails without the fix |
| CLI behavior change | Test in `ProjectStructureDetectorTest` or equivalent |
| Refactoring | Existing tests must still pass; add tests if coverage gaps are found |

### Test fixtures

Test fixtures live in `src/test/resources/fixtures/` within the relevant module. Organize them by rule or feature:

```
lang-java/src/test/resources/fixtures/
  nested-lookup/
    positive/         # code that should trigger the rule
      list-contains-in-for.java
    negative/         # code that should NOT trigger the rule
      set-contains-in-loop.java
```

Positive fixtures demonstrate the anti-pattern. Negative fixtures demonstrate similar-looking code that should not produce findings.

### Assertions

Use [Kotest matchers](https://kotest.io/docs/assertions/matchers.html) for all assertions:

```kotlin
result shouldBe expected
findings shouldHaveSize 1
findings.map { it.ruleId } shouldContainExactlyInAnyOrder listOf("nested-lookup")
```

## Documentation Is Required

Code changes that affect user-visible behavior must include documentation updates. Use this matrix to determine which pages to update:

| Change type | Pages to update |
|---|---|
| New rule | New rule page under `docs/rules/`, update `docs/rules/index.md` |
| New CLI flag or option | `docs/guide/cli-reference.md` |
| New behavior (project detection, caching, etc.) | Relevant guide page, `docs/getting-started/quickstart.md` if it changes the basic workflow |
| New build system or language support | `docs/guide/project-detection.md`, `docs/guide/multi-language.md` |
| Architecture decision | New ADR in `docs/adr/` |

### Documentation style

Follow these principles when writing documentation:

- **Write for the user.** Use "you" and active voice: "You can exclude directories with `--exclude`", not "Directories can be excluded by the system".
- **Lead with the task.** Start sections with what the user wants to do, not how the internals work: "To scan only error-level findings, pass `--severity error`".
- **One heading, one concept.** If a section covers two ideas, split it.
- **Show a code example for every feature.** A three-line example is worth a paragraph of explanation.
- **No jargon on first use.** If you use a term like "IR tree" or "call graph", explain it briefly or link to the Concepts section.
- **Keep pages focused.** A page should answer one question well. If it grows beyond that, split it into two pages.

### Documentation structure

The documentation follows the [Diataxis framework](https://diataxis.fr/):

| Section | Purpose | Style |
|---|---|---|
| Getting Started | Get a new user from zero to first result | Tutorial: step-by-step, concrete |
| Guide | Answer "how do I..." questions | How-to: task-oriented, practical |
| Concepts | Explain "how does it work" and "why" | Explanation: theory, context, trade-offs |
| Rules | Reference for each detection rule | Reference: complete, consistent format |
| Developer | Contributor documentation | Reference + how-to for contributors |

## Architecture Decisions

Use Architecture Decision Records (ADRs) for decisions that affect the project's direction or that future contributors might question. Examples:

- Choosing a minimum Java version
- Changing how project detection works
- Adding a new output format

ADRs live in `docs/adr/` and follow the [Nygard format](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions): Title, Status, Context, Decision, Consequences.

Naming: `NNNN-short-description.md` (e.g., `0001-use-java-11-as-minimum-target.md`).

## Build Verification

Before submitting a change, run the full build:

```bash
./gradlew build
```

This runs compilation, **detekt**, **ktlint**, and all tests in one command. The build must pass cleanly — no warnings-as-errors suppressions, no skipped checks.

Running only `./gradlew test` is **not sufficient** — it skips static analysis. Always use `build`.

If you have formatting issues that ktlint can auto-fix:

```bash
./gradlew ktlintFormat
```

### Pre-commit hook

The repository includes a pre-commit hook that runs detekt and ktlint before each commit. Set it up once after cloning:

```bash
git config core.hooksPath .githooks
```

This catches static analysis violations before they reach CI. The hook runs `detekt` and `ktlintCheck` — if either fails, the commit is blocked with instructions on how to fix it.
