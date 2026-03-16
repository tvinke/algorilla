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

!!! warning "Experimental"
    This feature is reserved for a future release. The configuration key is accepted but has no effect yet.

Algorilla infers variable types from declarations, constructors, factory methods, and stream chains to decide whether a `.contains()` or `.get()` call is O(1) (on a `HashSet`/`HashMap`) or O(n) (on a `List`). In most cases this works automatically.

When it lands, `type-hints` will let you manually tell algorilla the type of a variable it can't resolve — for example, a field injected by a framework or returned from an untyped factory:

```yaml
type-hints:
  userCache: "HashMap"    # injected by Spring, algorilla can't see the wiring
  itemIndex: "HashSet"    # returned from a factory method with Object return type
```

The key is the variable name, the value is the collection type. This prevents false positives where algorilla reports an O(n) lookup on what is actually an O(1) data structure.

### `heavyweight-types`

!!! warning "Experimental"
    This key is accepted but the format may change in future releases.

Controls which types the [`expensive-construction`](../rules/expensive-construction.md) rule flags when instantiated inside a method body (rather than once as a field or constant). Creating these objects is expensive — they typically involve reflection, class scanning, or configuration parsing — so repeated instantiation per call is a performance anti-pattern.

Algorilla ships with per-language defaults loaded from its internal semantics files. For Java, that includes types like `ObjectMapper`, `Pattern`, `SimpleDateFormat`, `Cipher`, `HttpClient`, and others. When you specify `heavyweight-types` in your config, your list **replaces** the defaults entirely:

```yaml
# REPLACES the built-in heavyweight types — include everything you care about
heavyweight-types:
  - ObjectMapper
  - Gson
  - XmlMapper
  - DocumentBuilderFactory
  - TransformerFactory
  - JAXBContext            # not in the defaults — add your own
```

If you only want to add types on top of the defaults, you need to list the defaults too. A common pattern is to copy the defaults and append your additions.

### `max-call-depth`

Maximum depth for following method calls during cross-file analysis. Default: 5.

## Language Filtering

Use `--language` (or `-l`) to restrict analysis to specific languages. Only matching source files will be scanned and only rules that apply to those languages will run.

```bash
# Single language
algorilla --language java src/

# Multiple languages (comma-separated)
algorilla --language java,groovy src/

# Multiple languages (repeated flag)
algorilla -l java -l kotlin src/
```

Available values: `java`, `groovy`, `kotlin`, `javascript`, `typescript` (case-insensitive).

`--language javascript` includes `.js`, `.mjs`, `.cjs`, `.jsx`, and `.vue` files. For Vue Single File Components, algorilla extracts the `<script>` block and analyzes it as JavaScript (or TypeScript when `<script lang="ts">` is used).

`--language typescript` includes `.ts` and `.tsx` files, plus Vue SFCs that use `<script lang="ts">`.

When `--language` is omitted, all supported languages and file formats are included (default behavior).

## CLI Override Precedence

Command-line options override file configuration:

1. CLI `--severity` overrides `min-severity`
2. CLI `--exclude` overrides `exclude`
3. CLI `--config` specifies a custom config file path
4. CLI `--language` filters source files and rules (no config file equivalent yet)
