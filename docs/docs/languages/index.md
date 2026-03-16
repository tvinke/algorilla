# Language Support

Algorilla analyzes source code in four language groups, each with its own parser. All 28 rules work across all languages unless noted otherwise.

## Supported Languages

<div class="grid cards" markdown>

-   :fontawesome-brands-java:{ .lg .middle } **Java** · GA

    Full ANTLR parser with complete AST. The most mature language support — production-ready with low false-positive rate.

    [:octicons-arrow-right-24: Java details](java.md)

-   :simple-kotlin:{ .lg .middle } **Kotlin** · Alpha

    Lightweight text-based parser with Kotlin-specific idiom recognition. Early support — higher false-positive rates expected.

    [:octicons-arrow-right-24: Kotlin details](kotlin.md)

-   :simple-apachegroovy:{ .lg .middle } **Groovy** · Beta

    ANTLR parser extending the Java grammar, with GDK method recognition. Usable with caveats — use `--confidence high` for best results.

    [:octicons-arrow-right-24: Groovy details](groovy.md)

-   :fontawesome-brands-js:{ .lg .middle } **JavaScript / TypeScript** · Beta

    Lightweight parser covering JS, TS, and Vue single-file components. Usable with caveats — use `--confidence high` for best results.

    [:octicons-arrow-right-24: JS/TS details](javascript.md)

</div>

## Rule Coverage Matrix

All rules work on the language-agnostic intermediate representation (IR), so most rules apply to all languages. Only rules that target platform-specific APIs are restricted.

| Rule | Java | Kotlin | Groovy | JS/TS |
|------|:----:|:------:|:------:|:-----:|
| `nested-lookup` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `repeated-linear-scan` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `sort-for-last` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `expensive-sort-comparator` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `filter-after-sort` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `bulk-load-for-single-lookup` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `n-plus-one-query` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `expensive-construction` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `repeated-regex-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `regex-recompilation-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `expensive-serialization-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `sequential-async-join-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `in-loop-collection-building` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `string-concat-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `quadratic-removal` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `hidden-nested-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `io-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `expensive-callback` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `cardinality-explosion` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `lazy-loading-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `redundant-expensive-call` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `unmemoized-recursion` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `repeated-collection-iteration` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `loop-invariant-hoisting` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `uncached-getter` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `chained-getters` | :white_check_mark: | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| `repeated-reflection-in-loop` | :white_check_mark: | :white_check_mark: | :white_check_mark: | |
| `parallel-pipeline-bottleneck` | :white_check_mark: | :white_check_mark: | :white_check_mark: | |
