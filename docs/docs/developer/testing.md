# Testing

## Test Structure

```
src/test/
├── kotlin/.../                  # Test classes
└── resources/fixtures/
    ├── nested-lookup/
    │   ├── positive/            # Files that trigger findings
    │   │   ├── list-contains.java
    │   │   └── stream-anyMatch.java
    │   └── negative/            # Files that should NOT trigger findings
    │       ├── set-contains.java
    │       └── map-lookup.java
    └── sort-for-last/
        ├── positive/
        └── negative/
```

## Writing Tests

### Unit Tests

Test individual components (parser, rule logic) in isolation:

```kotlin
@Test
fun `should parse for-each loop`() {
    val tree = parser.parse(fixturePath("parser/for-each.java"))
    val loops = tree.findDescendants<LoopNode>()
    loops shouldHaveSize 1
    loops[0].kind shouldBe LoopKind.FOR_EACH
}
```

### Integration Tests

Test the full pipeline on fixture files:

```kotlin
@Test
fun `should detect contains inside forEach`() {
    val result = analyzeFixture("nested-lookup/positive/list-contains.java")
    result.findings shouldHaveSize 1
    result.findings[0].ruleId shouldBe "nested-lookup"
}
```

## Conventions

- **Fixture naming**: `<rule-name>-<variant>.<ext>`
- **Generic names**: Use `Order`, `Product`, `User`, `Item` — not domain-specific names
- **Test names**: Describe expected behavior — `"should detect contains() inside forEach loop"`
- **Parameterized tests**: Use JUnit 5 `@ParameterizedTest` with `@MethodSource` for fixture-driven tests

## Running Tests

```bash
./gradlew check          # All tests + Detekt + ktlint
./gradlew test           # Tests only
./gradlew :core:test     # Tests for a specific module
```
