# Uncached Getter

**Rule ID:** `uncached-getter` · **Severity:** INFO · **Complexity:** k·O(lookup) → O(lookup)

## Description

Detects getter-style calls invoked multiple times with the same argument in a function. When a method like `getAnimal(id)` is called repeatedly with the same ID, the result should be stored in a local variable.

## Bad Example

```java
public void enrichRegistration(Registration reg) {
    String name = getAnimal(reg.getAnimalId()).getName();
    String breed = getAnimal(reg.getAnimalId()).getBreed(); // Same call
    String owner = getAnimal(reg.getAnimalId()).getOwner(); // Same call again
    // ...
}
```

## Good Example

```java
public void enrichRegistration(Registration reg) {
    Animal animal = getAnimal(reg.getAnimalId());
    String name = animal.getName();
    String breed = animal.getBreed();
    String owner = animal.getOwner();
}
```

## How Detection Works

1. Identifies calls with getter-pattern prefixes: `get`, `find`, `load`, `fetch`, `lookup`, `resolve`
2. Groups by target + method name + argument key
3. Flags groups with 2 or more identical calls
4. Excludes short generic names like `get`, `getOrDefault`

## Suggestion

> Cache the result in a local variable

## Related Rules

- [Redundant Expensive Call](redundant-expensive-call.md) — broader pattern for any repeated call
- [Chained Getters](chained-getters.md) — cascading lookups where one feeds the next
