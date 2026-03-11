# Multi-Language Support

Algorilla analyzes code in multiple languages using a unified intermediate representation (IR).

## Supported Languages

| Language | Extensions | Parser |
|----------|-----------|--------|
| Java | `.java` | ANTLR 4 |
| Groovy | `.groovy` | ANTLR 4 (Java grammar + preprocessing) |
| Kotlin | `.kt` | ANTLR 4 (Java grammar + preprocessing) |
| JavaScript | `.js` | Regex-based scanner |
| TypeScript | `.ts` | Regex-based scanner |
| Vue SFC | `.vue` | Script block extraction + JS/TS scanner |

## How It Works

Each language parser converts source code into language-agnostic IR nodes. Rules operate on the IR, so a single rule like `nested-lookup` works across all languages without modification.

### Groovy

Groovy is parsed using the Java ANTLR grammar with preprocessing that transforms Groovy-specific syntax (`def`, `@CompileStatic`, etc.) into Java-compatible code. Groovy-specific iteration methods (`each`, `collect`, `findAll`) are recognized as loop constructs.

### Kotlin

Kotlin is preprocessed to transform `fun`, `val`/`var`, and Kotlin parameter syntax into Java-compatible form before parsing with the Java grammar.

### JavaScript/TypeScript

JS/TS files are scanned with a regex-based approach that recognizes function declarations, loops, method calls, and object creation patterns. Vue SFC files have their `<script>` blocks extracted and analyzed as JS/TS.

## Mixed Codebases

A single `algorilla` invocation handles mixed-language projects. Point it at your project root and it automatically detects file types:

```bash
algorilla /path/to/mixed-language-project
```
