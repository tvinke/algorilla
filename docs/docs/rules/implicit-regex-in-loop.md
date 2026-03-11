# Implicit Regex in Loop

**Rule ID:** `implicit-regex-in-loop` · **Severity:** WARNING · **Complexity:** O(n·compile) → O(n)

## Description

Detects `String` methods that internally compile a regular expression on every call when used inside loops. Methods like `matches()`, `split()`, `replaceAll()`, and `replaceFirst()` call `Pattern.compile()` under the hood — compiling the same regex pattern on every iteration wastes CPU.

This rule is a companion to [`repeated-regex-in-loop`](repeated-regex-in-loop.md), which detects explicit `Pattern.compile()` calls. This rule catches the implicit variant hiding inside innocent-looking `String` methods.

## Bad Example

=== "Java"

    ```java
    for (Animal animal : animals) {
        if (animal.getName().matches("^[A-Z][a-z]+$")) {     // Pattern compiled every iteration
            validAnimals.add(animal);
        }
    }
    ```

=== "Groovy"

    ```groovy
    animals.each { animal ->
        def parts = animal.diet.split("\\s*,\\s*")           // Pattern compiled every iteration
        parts.each { dietItems.add(it.trim()) }
    }
    ```

## Good Example

=== "Java"

    ```java
    Pattern namePattern = Pattern.compile("^[A-Z][a-z]+$");
    for (Animal animal : animals) {
        if (namePattern.matcher(animal.getName()).matches()) { // Compiled once
            validAnimals.add(animal);
        }
    }
    ```

=== "Groovy"

    ```groovy
    def dietSplitter = Pattern.compile("\\s*,\\s*")
    animals.each { animal ->
        def parts = dietSplitter.split(animal.diet)           // Compiled once
        parts.each { dietItems.add(it.trim()) }
    }
    ```

## Suggestion

> Pre-compile with Pattern.compile() outside the loop and use Matcher directly

## Affected Methods

| Method | What it does internally |
|--------|----------------------|
| `String.matches(regex)` | `Pattern.compile(regex).matcher(this).matches()` |
| `String.split(regex)` | `Pattern.compile(regex).split(this)` |
| `String.replaceAll(regex, replacement)` | `Pattern.compile(regex).matcher(this).replaceAll(replacement)` |
| `String.replaceFirst(regex, replacement)` | `Pattern.compile(regex).matcher(this).replaceFirst(replacement)` |
