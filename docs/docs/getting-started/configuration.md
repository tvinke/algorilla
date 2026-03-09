# Configuration

Algorilla works without any configuration. For customization, create a `.algorilla.yml` file in your project root (next to `build.gradle.kts`, `pom.xml`, or `package.json`).

Algorilla also stores its analysis cache in a `.algorilla/` directory in the project root. You may want to add this to your `.gitignore`:

```gitignore
.algorilla/
```

## Example Configuration

```yaml
# Minimum severity to report (info, warning, error)
min-severity: warning

# Glob patterns to exclude from analysis
exclude:
  - "**/generated/**"
  - "**/legacy/**"

# Maximum call depth for cross-file analysis
max-call-depth: 5

# Per-rule overrides
rules:
  nested-lookup:
    enabled: true
    severity: error
  expensive-construction:
    enabled: false

# Type hints for collections the tool can't resolve
type-hints:
  userCache: "HashMap"
  itemIndex: "HashSet"

# Custom heavyweight types to detect
heavyweight-types:
  - ObjectMapper
  - Gson
  - XmlMapper
  - DocumentBuilderFactory
  - TransformerFactory
  - JAXBContext
```

## Configuration Options

### `min-severity`

Minimum severity level to include in the report. Options: `info`, `warning`, `error`.

### `exclude`

List of glob patterns for files or directories to exclude from scanning. These are applied in addition to the automatic exclusions (node_modules, build directories, test code).

### `rules`

Per-rule configuration overrides:

- `enabled` — Set to `false` to disable a specific rule
- `severity` — Override the default severity for a rule

### `type-hints`

Manual type hints for variables whose types cannot be resolved statically. Map variable names to collection types (e.g., `HashSet`, `HashMap`) to prevent false positives on O(1) collection operations.

### `heavyweight-types`

Set of class names considered "heavyweight" for the heavyweight-object-per-invocation rule. Defaults include `ObjectMapper`, `Gson`, `XmlMapper`, `DocumentBuilderFactory`, and `TransformerFactory`.

### `max-call-depth`

Maximum depth for following method calls during cross-file analysis. Default: 5.

## CLI Override Precedence

Command-line options override file configuration:

1. CLI `--severity` overrides `min-severity`
2. CLI `--exclude` overrides `exclude`
3. CLI `--config` specifies a custom config file path
