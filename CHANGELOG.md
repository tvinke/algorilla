# Changelog

## [0.3.0](https://github.com/tvinke/algorilla/compare/v0.2.0...v0.3.0) (unreleased)

### BREAKING CHANGES

* **`--fail-on` default changed from `info` to `warning`.** If you relied on info-level findings failing your CI build, add `--fail-on info` explicitly. The GitHub Action default changed accordingly. ([#80](https://github.com/tvinke/algorilla/issues/80))

### Features

* **API stability & versioned formats** — JSON output now includes `schemaVersion` and `algorillaVersion` fields; baseline and ignore-list files include a `version` field. Consumers should ignore unknown fields for forward compatibility. ([#80](https://github.com/tvinke/algorilla/issues/80))
* **Lenient config parsing** — `.algorilla.yml` silently ignores unknown keys, so configs written for newer versions don't crash older ones ([#80](https://github.com/tvinke/algorilla/issues/80))
* **Experimental config key warnings** — stderr notice when `type-hints` or `heavyweight-types` are used in config
* **`--list-rules` shows stability tier** per rule
* **Framework-aware semantics overlays** — language semantics registry now supports per-framework method packs (Spring, Guava, etc.) loaded from YAML ([927e2dd](https://github.com/tvinke/algorilla/commit/927e2dd))
* **Exhaustive stdlib coverage** — collection semantics expanded with full stdlib method coverage across Java, Kotlin, Groovy, and JS/TS ([87a4ed9](https://github.com/tvinke/algorilla/commit/87a4ed9))
* **JS/TS variable type inference** — nested-lookup rule now infers variable types from initializers, reducing false positives on Set/Map usage ([cab5575](https://github.com/tvinke/algorilla/commit/cab5575))
* **Floating `v0` tag** in release workflow — GitHub Action users can use `@v0` for auto-updates within the 0.x line
* Language tags on rule documentation pages ([08bf1a9](https://github.com/tvinke/algorilla/commit/08bf1a9))
* Stability & compatibility documentation page with tier classification, deprecation policy, and 1.0 readiness criteria

### Bug Fixes

* Skip `implicit-regex` findings for string literal arguments in JS/TS — `"hello".replace("x", "y")` is not a regex ([11a8132](https://github.com/tvinke/algorilla/commit/11a8132))
* Treat small inline array lookups (e.g. `["a","b"].includes(x)`) as O(1) in JS parser ([087e436](https://github.com/tvinke/algorilla/commit/087e436))
* Fix color rendering of language tags in docs ([7a5a97a](https://github.com/tvinke/algorilla/commit/7a5a97a))
* npm publish is now idempotent on re-tag ([844c91e](https://github.com/tvinke/algorilla/commit/844c91e))

### Maintenance

* Renamed `CollectionSemanticsRegistry` to `LanguageSemanticsRegistry` to reflect broader scope ([2f0069b](https://github.com/tvinke/algorilla/commit/2f0069b))
* Moved hardcoded framework constants into centralized YAML semantics files ([bdf0b51](https://github.com/tvinke/algorilla/commit/bdf0b51))
* Added compatibility tests for config, baseline, ignore-list, and JSON output formats
* Added issue templates, PR template, security policy, and code of conduct ([711cd28](https://github.com/tvinke/algorilla/commit/711cd28), [1ecfb0a](https://github.com/tvinke/algorilla/commit/1ecfb0a))
* Docs: restructured navigation, horizontal tabs, rule subsumption docs, cleaned up rule doc pages

## [0.2.0](https://github.com/tvinke/algorilla/compare/v0.1.0...v0.2.0) (2026-03-11)


### Features

* add --language flag to filter by language ([1988578](https://github.com/tvinke/algorilla/commit/1988578e17c29d4c0f643c0170ad57b6543ab067)), closes [#10](https://github.com/tvinke/algorilla/issues/10)
* add aliases field to Rule interface ([91ad2c5](https://github.com/tvinke/algorilla/commit/91ad2c5c5336a84ddfdd905954bc238bb39715f3)), closes [#41](https://github.com/tvinke/algorilla/issues/41)
* add filter-after-sort rule ([60bd2ce](https://github.com/tvinke/algorilla/commit/60bd2ce344a59d46a613d0a7553ddfe4ec966d5e))
* add numeric parsing, servlet, and time accessor methods to cheap-methods ([d22ab9c](https://github.com/tvinke/algorilla/commit/d22ab9cd92ec2732bf497516e032de1e82699f02))
* add parallel-stream-bottleneck rule for concurrency anti-patterns ([0813f74](https://github.com/tvinke/algorilla/commit/0813f742058a6814a76cc8dfd94151b32b90f087))
* add project structure detection and auto-exclude test code ([82177fa](https://github.com/tvinke/algorilla/commit/82177faa315df11f13c1893787274170fd0f146b))
* add rule categories for grouping and filtering ([3273363](https://github.com/tvinke/algorilla/commit/327336302c2eb3a052883eb105edc79e4b39e27b)), closes [#40](https://github.com/tvinke/algorilla/issues/40)
* add suggested fix diffs to findings ([b066d50](https://github.com/tvinke/algorilla/commit/b066d504d1b07f64016dc6df84259d511b436ab2)), closes [#39](https://github.com/tvinke/algorilla/issues/39)
* auto-detect project root and source directories ([8afceb5](https://github.com/tvinke/algorilla/commit/8afceb587393614d62d2d237108b2163f8202ac3))
* catch addAll/concat/putAll inside loops ([8269ca0](https://github.com/tvinke/algorilla/commit/8269ca02615b522c5519704e48123688cceb5806)), closes [#22](https://github.com/tvinke/algorilla/issues/22)
* chained getter cascade detection ([ea5aa93](https://github.com/tvinke/algorilla/commit/ea5aa931f79fe29e3d710ffd57f198c80ebe8ce1)), closes [#24](https://github.com/tvinke/algorilla/issues/24)
* class-qualified method resolution across all language visitors ([08e202c](https://github.com/tvinke/algorilla/commit/08e202cffaa21a31e63f63f18a1f7587ca66f0cc))
* cross-method analysis for sort comparator rules ([607f16c](https://github.com/tvinke/algorilla/commit/607f16ce65959e77471eb468c4f14fa95210496a)), closes [#35](https://github.com/tvinke/algorilla/issues/35)
* cross-method sort-for-last + expensive callback detection ([4efc0b7](https://github.com/tvinke/algorilla/commit/4efc0b76e50221b0c9c63bfa94b8d5343b779c82))
* data-driven collection semantics registry ([ddc1b24](https://github.com/tvinke/algorilla/commit/ddc1b24050922431a5ec3ae4956690716b07f921)), closes [#14](https://github.com/tvinke/algorilla/issues/14)
* detect hidden nested loops across method boundaries ([#56](https://github.com/tvinke/algorilla/issues/56)) ([df80488](https://github.com/tvinke/algorilla/commit/df80488f74e7f72b77ef45fc5ae77c6fc74ee5bc))
* detect regex compilation inside loops ([c8d692b](https://github.com/tvinke/algorilla/commit/c8d692bc4e4b1704148e22e15459917d5d50b9b3)), closes [#20](https://github.com/tvinke/algorilla/issues/20)
* detect same call with same args invoked multiple times ([724b1af](https://github.com/tvinke/algorilla/commit/724b1af3295626c05062bc8b912d6842f5b88ce9)), closes [#25](https://github.com/tvinke/algorilla/issues/25)
* detect sequential .join()/.get() on futures in loops ([787cd7e](https://github.com/tvinke/algorilla/commit/787cd7e521b4b9ad443064e5f6cd4bdf3ca30aa3)), closes [#21](https://github.com/tvinke/algorilla/issues/21)
* flag serialization calls in loops ([9e4e252](https://github.com/tvinke/algorilla/commit/9e4e2529629e73abf0c65fdb4babf91c69e2f29c)), closes [#19](https://github.com/tvinke/algorilla/issues/19)
* group console output by file ([ce1f131](https://github.com/tvinke/algorilla/commit/ce1f131ef647e06ac89ddf2ac3f24b48a049e67a)), closes [#38](https://github.com/tvinke/algorilla/issues/38)
* implement all 7 analysis rules ([3910d36](https://github.com/tvinke/algorilla/commit/3910d36c80f9f8a823accb29c586f70dd890fceb))
* implement cross-file call graph and complexity propagation ([8e76e4f](https://github.com/tvinke/algorilla/commit/8e76e4f79d000dea7e0ba6f1c45dffabd71e8e50))
* implement custom rules via Kotlin Script DSL ([4ac8402](https://github.com/tvinke/algorilla/commit/4ac840251afe452a9effad5f9d256f76ad3f15a6))
* implement Groovy parser using Java ANTLR grammar ([c0be438](https://github.com/tvinke/algorilla/commit/c0be438a92e3db3522c3b4ffa63897091d96dbec))
* implement incremental caching and baseline diff mode ([d6a09e5](https://github.com/tvinke/algorilla/commit/d6a09e500cc5646ae9d586c4c8c5d47aa8fedc08))
* implement Java parser with ANTLR grammar ([62ba5d0](https://github.com/tvinke/algorilla/commit/62ba5d08834cd532418bdad29e1d69c63664accd))
* implement JavaScript/TypeScript and Kotlin parsers ([b807d07](https://github.com/tvinke/algorilla/commit/b807d07bb6d8518771b351b37aa047ca4af5473c))
* implement nested-lookup rule with end-to-end pipeline ([ae13086](https://github.com/tvinke/algorilla/commit/ae13086ee9a693f5b0adb23291024270d0f133e0))
* implement SARIF and JSON output formats ([fd5b52f](https://github.com/tvinke/algorilla/commit/fd5b52f547200d1d80fb74ffcc429d81f5e70054))
* implement YAML configuration and inline suppression ([aa319eb](https://github.com/tvinke/algorilla/commit/aa319ebe5b3f37ef3ab85fa0f54dca51f7087eb6))
* initial project setup ([eaaa03f](https://github.com/tvinke/algorilla/commit/eaaa03f6465c1965ba0dfa8c8105549986947001))
* lower JVM target to Java 11 for broader adoption ([9e2c3eb](https://github.com/tvinke/algorilla/commit/9e2c3ebc3e9940077f9704ff363fd743ad364aef))
* N+1 repository call detection ([ee75264](https://github.com/tvinke/algorilla/commit/ee75264230943537e078ed5b194d155508cb4ce9)), closes [#18](https://github.com/tvinke/algorilla/issues/18)
* treat iterating lookups as iteration context in nested-lookup rule ([88842f6](https://github.com/tvinke/algorilla/commit/88842f6a5092ec5dc6e6a3c61a88ffe9b03dc6e0)), closes [#34](https://github.com/tvinke/algorilla/issues/34)
* type-aware SymbolTable for cross-method resolution ([9530e1a](https://github.com/tvinke/algorilla/commit/9530e1ae7c141f0d332ba5bf55bf6e2e45594969)), closes [#59](https://github.com/tvinke/algorilla/issues/59)
* uncached getter detection ([7fcef3c](https://github.com/tvinke/algorilla/commit/7fcef3c8fc5f2fc1f1981956522f13cb7cca9941)), closes [#23](https://github.com/tvinke/algorilla/issues/23)
* widen N+1 detection to catch findByX patterns without suffix whitelist ([ea90687](https://github.com/tvinke/algorilla/commit/ea906877a2acc99bedb20fd20e395704f8eb7949))


### Bug Fixes

* reduce false positives from redundant-call and getter rules ([9f9c740](https://github.com/tvinke/algorilla/commit/9f9c74035ca2676df8b3278151efb35d4bf05b10))
* redundant-expensive-call was ignoring argument values ([d2ccc1b](https://github.com/tvinke/algorilla/commit/d2ccc1baa1e56e0b94fcc43a52e908f2ba701176)), closes [#57](https://github.com/tvinke/algorilla/issues/57)
* skip heavyweight detection in constructors ([6e86a3b](https://github.com/tvinke/algorilla/commit/6e86a3b3e00004b539a0411daebac46251015911)), closes [#36](https://github.com/tvinke/algorilla/issues/36)
* skip recursive methods in hidden-nested-loop detection ([8a6ae82](https://github.com/tvinke/algorilla/commit/8a6ae82ba666fdef89f1513f21613757a43daa09))
* tighten FP filters — registry gaps, purity model, arg fingerprinting ([31f16cf](https://github.com/tvinke/algorilla/commit/31f16cfa1daab47de230774fb5737da20374f6f5))
* use method call location for chained calls ([8b19878](https://github.com/tvinke/algorilla/commit/8b19878948292f27dd8f747948f902ffa81e1c26))

## 0.1.0 (2026-03-09)

First release of algorilla.

### Rules

15 built-in rules for detecting algorithmic complexity anti-patterns:

| Rule | What it catches |
|------|----------------|
| `nested-lookup` | Linear lookup inside loop body (O(n*m) → O(n+m)) |
| `repeated-linear-scan` | Multiple linear scans on same collection |
| `sort-for-last` | Sorting entire collection just to get first/last element |
| `expensive-sort-comparator` | Linear search, date parsing, or heavy objects in sort comparator |
| `filter-after-sort` | `sorted().filter()` — filtering after sorting wastes effort |
| `bulk-load-for-single-lookup` | `findAll()` + filter when a targeted query would do |
| `expensive-construction` | ObjectMapper/Gson/SimpleDateFormat in method body |
| `n-plus-one-query` | Single-record DAO fetch or countBy inside a loop |
| `repeated-regex-in-loop` | Regex compilation inside loop |
| `expensive-serialization-in-loop` | Serialization/deserialization inside loop |
| `sequential-async-join-in-loop` | Blocking `.join()`/`.get()` on futures in loop |
| `in-loop-collection-building` | `addAll()`/`concat()` inside loop |
| `redundant-expensive-call` | Same call with same args invoked multiple times |
| `uncached-getter` | Getter-pattern call repeated with same argument |
| `chained-getters` | Cascading getter chain where each feeds into the next |

### Languages

- Java (ANTLR parser)
- Groovy (ANTLR parser, Groovy-specific GDK method recognition)
- Kotlin (lightweight text-based parser)
- JavaScript / TypeScript / Vue (lightweight text-based parser)

### Features

- **Rule categories** — Every rule belongs to a category (Loop amplifiers, Sort abuse, Redundancy, Construction cost, Query patterns). Console output shows the category tag per finding.
- **Rule aliases** — Rules declare aliases for backwards compatibility. Old IDs in suppress comments and config keep working after renames.
- **Suggested fixes** — Sort-for-last, expensive-construction, and filter-after-sort include fix suggestions in console and SARIF output.
- **`--language` filter** — `-l` / `--language` option to restrict analysis to specific languages.
- **Cross-method analysis** — Rules follow method references one level deep to detect indirect patterns.
- **Collection Semantics Registry** — YAML-based method classification per language, extensible via config.
- **Method purity model** — Side-effect classification for smarter redundant-call detection.
- Incremental analysis with content-hash based caching
- Baseline mode for CI (only report new findings)
- Inline suppression via `// algorilla:ignore [rule-id]`
- Custom rules via Kotlin DSL (`.algorilla/rules/*.kts`)
- User-extensible configuration via `.algorilla.yml`
- Auto-detection of project root and source directories

### Output formats

- Console (grouped by file, with evidence chains)
- SARIF v2.1 (GitHub Code Scanning compatible)
- JSON

### CI/CD

- GitHub Actions CI (Java 11, 17, 21 matrix)
- Release automation via Release Please
- CI/CD integration guide and pre-commit hook guide in docs
