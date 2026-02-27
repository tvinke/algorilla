# Adding Languages

## Implement the Parser

1. Create a new module (e.g., `lang-python/`)

2. Implement the `LanguageParser` interface:

```kotlin
class PythonParser : LanguageParser {
    override val language = Language.PYTHON
    override fun canParse(filePath: String) = filePath.endsWith(".py")

    override fun parse(filePath: String): FileRoot {
        // Parse the file and produce IR nodes
        val children = parseFile(filePath)
        return FileRoot(
            filePath = filePath,
            language = Language.PYTHON,
            location = SourceLocation(filePath, 1, 1),
            children = children,
        )
    }
}
```

3. Add the language to the `Language` enum in `core/model/Language.kt`.

4. Register the parser in `AlgorillaCommand.kt`.

## IR Mapping

Map language constructs to the unified IR:

- Loops → `LoopNode` with appropriate `LoopKind`
- Collection operations → `LookupCall`, `SortCall`, `CollectionAccess`
- Functions → `FunctionDecl`
- Method calls → `FunctionCall`
- Object creation → `ObjectCreation`
- Variables → `VariableDecl`

## Testing

- Create parser unit tests with language-specific fixtures
- Run integration tests to verify rules work with the new language
- Test with a real-world codebase in the target language
