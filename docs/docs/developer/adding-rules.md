# Adding Rules

## Built-in Rule

Create a new class in `core/src/main/kotlin/.../rules/builtin/` implementing the `Rule` interface. The interface requires five fields (`id`, `name`, `severity`, `languages`, `category`) and one method (`evaluate`). Two optional fields have defaults: `aliases` (empty list) and `subsumes` (empty set).

See `Rule.kt` for the full interface definition. Use any existing rule as a starting point — `NestedLookupRule.kt` is a good reference for the standard pattern.

Register the rule in `BuiltinRules.kt` (same package) by adding it to the `all()` list.

??? example "What the Rule interface looks like"

    From `core/src/main/kotlin/.../rules/Rule.kt`:

    - `id` — unique kebab-case identifier, e.g. `"nested-lookup"`
    - `name` — human-readable, e.g. `"Nested Lookup"`
    - `severity` — `Severity.WARNING`, `INFO`, or `ERROR`
    - `languages` — which languages this rule applies to (use `Language.entries.toSet()` for all)
    - `category` — one of the `RuleCategory` values (`LOOP_AMPLIFIER`, `SORT_ABUSE`, `REDUNDANCY`, etc.)
    - `evaluate(context: AnalysisContext): List<Finding>` — the detection logic

    Most rules follow the same scan pattern: iterate `context.irTrees`, recursively walk each `FileRoot`, check nodes against the pattern, produce `Finding` objects.

## Subsumption

If your rule detects a pattern that overlaps with an existing rule — meaning your rule is a strictly more specific version — declare which rule(s) it subsumes via the `subsumes` field. When both rules fire at the same source location, the engine keeps your finding and drops the subsumed one. The subsumed rule's findings at other locations are unaffected.

!!! example "nested-lookup subsumes expensive-callback"

    `expensive-callback` detects any expensive operation inside a callback (date parsing, object creation, linear lookup, etc.). `nested-lookup` specifically detects linear lookups inside loops/callbacks — a strictly narrower pattern. When both fire on the same `list.contains()` inside a `forEach`, the more specific `nested-lookup` finding wins.

    See `NestedLookupRule.kt` line 34: `override val subsumes: Set<String> = setOf("expensive-callback")`

!!! example "redundant-expensive-call subsumes uncached-getter"

    `uncached-getter` flags repeated getter calls with the same argument (`findById(id)` called 3 times). `redundant-expensive-call` detects any repeated expensive call with the same arguments — a broader detection that fully covers what `uncached-getter` finds.

    See `RedundantExpensiveCallRule.kt` line 31: `override val subsumes: Set<String> = setOf("uncached-getter")`

!!! warning "When NOT to use subsumption"

    `string-concat-in-loop` and `in-loop-collection-building` both fire inside loops, but they detect fundamentally different problems (string allocation vs. collection growth). Neither subsumes the other — both findings should survive even when they co-locate.

Guidelines:

- Only declare subsumption when your rule is strictly more specific. If the rules detect genuinely different problems that happen to co-locate, both findings should survive.
- Subsumption is directional — the more specific rule declares the relationship, not the general one.
- Check existing `subsumes` declarations in the codebase to avoid conflicts.

See [Architecture: Rule Subsumption](architecture.md#rule-subsumption) for how the engine implements this.

## Confidence

Rules declare their baseline confidence via `defaultConfidence` on the Rule interface:

```kotlin
override val defaultConfidence: Confidence = Confidence.HIGH  // structurally certain
```

- **HIGH** — the pattern is always an anti-pattern regardless of types (e.g. string concat in loop)
- **MEDIUM** — reliable but may depend on type context (default for most rules)
- **LOW** — heuristic-heavy, expect false positives (hidden at default `--confidence medium`)

Rules can also set confidence **per finding** when they have evidence for a specific instance:

```kotlin
Finding(
    ...
    confidence = if (flowConfirmed) Confidence.HIGH else Confidence.MEDIUM,
)
```

The engine respects per-finding confidence (doesn't override non-MEDIUM values). It only adjusts findings still at MEDIUM — demoting to LOW in languages without type declarations when `requiresTypeContext = true`.

## Parameter flow

The engine annotates each `FunctionDecl` with `parameterFlows` — a list of where each parameter ends up (loops, method calls, function arguments). Rules can use this to verify findings with structural proof:

```kotlin
// Check if the lookup target is backed by a function parameter
val paramBacked = fn.parameterFlows.any { it.paramName == targetVar }
val confidence = if (paramBacked) Confidence.HIGH else Confidence.MEDIUM
```

For cross-method detection, use `ParameterFlowQuery`:

```kotlin
// Does a caller's parameter flow through this call into an IO operation?
val evidence = ParameterFlowQuery.parameterFlowsThrough(
    call, callerFn, context.symbolTable,
) { it is FlowTarget.MethodCallReceiver && it.methodName in ioMethods }
```

See `NestedLookupRule` and `HiddenNestedLoopRule` for examples of flow-based confidence upgrades.

## Semantics-driven detection

Rules should not hardcode method names. Instead, method categories are read from the semantics YAML files at startup and stored in `LanguageSemanticsRegistry`. Use `AnalysisContext.semantics` to look up whether a method is expensive, a stream operation, etc.

This means adding a new framework method to the relevant YAML file automatically makes existing rules detect it — no rule code changes needed.

??? example "How rules use semantics"

    See any rule that checks method types — e.g. `NestedLookupRule` uses `isCollectionLookup()` (from `core/util/`), which delegates to the semantics registry. The registry is populated from the YAML files under `core/src/main/resources/semantics/`.

## Testing

Add test fixtures under `src/test/resources/fixtures/<rule-id>/`:

- `positive/` — source files that should trigger at least one finding from this rule
- `negative/` — source files that should trigger no finding from this rule

Write a test class that runs the full pipeline (parse → IR → rule → finding) for each fixture.

??? tip "Test pattern to follow"

    The Java rule tests in `lang-java/src/test/kotlin/` follow a consistent pattern: load a fixture file, parse it, run the rule, assert on findings. See `NestedLookupRuleJavaTest.kt` or `HeavyweightObjectPerInvocationRuleJavaTest.kt` for the template.

    Kotlin and Groovy tests in their respective `lang-*/src/test/kotlin/` directories follow the same structure.

Cover at least:

- One or more positive cases per variant of the pattern
- Negative cases for the most common false-positive triggers (e.g. same method called once, or inside a non-loop context)
- Evidence fields (file, line, column) in positive cases

Run `./gradlew build` before opening a PR. The build runs ktlint, detekt, and all tests.

## Custom Rule via DSL

See [Custom Rules](../guide/custom-rules.md) for the Kotlin Script approach, which lets users define rules outside the core module.
