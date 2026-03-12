# Stability & Compatibility

Algorilla follows [Semantic Versioning](https://semver.org/). During the **0.x phase**, minor versions may include breaking changes, but we try hard to avoid them and document them prominently in the CHANGELOG when they happen.

## Stability Tiers

Every part of the public surface is classified into one of four tiers.

### Stable

**Promise:** only changed with a deprecation cycle and a CHANGELOG entry marked `BREAKING`.

- CLI flag names (`--severity`, `--fail-on`, `--format`, etc.)
- Exit codes: `0` (clean), `1` (findings matched `--fail-on`), `2` (internal error)
- Config file name (`.algorilla.yml`) and top-level keys (`rules`, `exclude`, `min-severity`, `max-call-depth`)
- Rule IDs (e.g. `nested-lookup`, `sort-for-last`)
- Severity values: `info`, `warning`, `error`
- Output format names: `console`, `json`, `sarif`
- JSON output field names (existing fields are never removed or renamed)
- Baseline fingerprint algorithm
- Gradle plugin extension properties (`minSeverity`, `failOn`, `format`, `outputFile`, `excludePatterns`, `rules`, `includeTests`, `baseline`)

### Evolving

**Promise:** new additions are always safe. Removals only happen via deprecation.

- JSON output (new fields may appear — consumers MUST ignore unknown fields)
- GitHub Action inputs/outputs (new inputs may be added)
- Gradle plugin extension (new properties may be added)
- Rule categories (new categories may be added)
- `Rule` interface (new methods may be added with default implementations)

### Experimental

**Promise:** documented, but may change in any 0.x release without deprecation.

- `type-hints` config key
- `heavyweight-types` config key

A stderr notice is printed when experimental config keys are used.

### Internal

**No stability promise.** Not documented, not part of the public API.

- Custom rule DSL internals
- IR node types
- Programmatic core API (the `core` module)
- Cache file format
- Console output formatting
- Kotlin classes marked `internal`

## Tier Promotion

| Transition | Criteria |
|---|---|
| Experimental to Evolving | Survived 2+ minor releases without changes, seen actual adoption |
| Evolving to Stable | Survived 2+ minor releases, no removals or renames needed |
| Stable to Deprecated | Only via deprecation cycle (see below) |

## Deprecation Policy

### Pre-1.0

A single minor release of deprecation notice is acceptable. The old behavior continues to work in the release that introduces the deprecation, and may be removed in the next minor release.

### Post-1.0

Minimum 2 minor releases between deprecation notice and removal.

### How deprecation works by surface type

**CLI flags:** old flag keeps working, new flag is introduced, stderr warning printed. Removed after deprecation period.

**Config keys:** both old and new key accepted, warning on the old key. Removed after deprecation period.

**Rule IDs:** retired rules stay registered (producing no findings) so configs and baselines don't break. The `aliases` mechanism handles renames transparently.

**JSON output fields:** new field added alongside old, old field documented as deprecated. Never remove a field without a `schemaVersion` bump.

## JSON Output Contract

If you consume the JSON output (`--format json`), follow these rules for forward compatibility:

1. **Ignore unknown fields.** New fields may appear in any release.
2. **Check `schemaVersion`.** A bump signals structural changes.
3. **`algorillaVersion`** tells you which version produced the output.

## Versioned Formats

All serialized formats include a `version` field for future evolution:

| Format | Version field | Current value |
|--------|--------------|---------------|
| JSON output | `schemaVersion` | `1` |
| Baseline (`.json`) | `version` | `1` |
| Ignore list (`.json`) | `version` | `1` |
| Config (`.algorilla.yml`) | `version` (optional, absence = 1) | `1` |

## Config Compatibility

`.algorilla.yml` parsing is lenient: **unknown keys are silently ignored**. This means a config file written for a newer version of Algorilla will not crash an older version — the unrecognized keys are simply skipped.

## 1.0 Readiness Criteria

Algorilla will move to 1.0 when all of these hold:

1. JSON schema has survived 2+ minor versions without structural changes
2. Config schema has survived 2+ minor versions without breaking changes
3. No CLI flag renames or removals in the last 2 releases
4. People other than the author are actually using it 😉
