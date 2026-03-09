# N+1 Query

**Rule ID:** `n-plus-one-query` · **Severity:** WARNING · **Complexity:** O(n·IO) → O(1·IO + n)

## Description

Detects single-record fetch calls (`findById()`, `getById()`, `countBy*`) inside loops. Each iteration triggers a separate database or service call, turning what could be a single bulk fetch into N sequential round-trips.

## Bad Example

=== "Java"

    ```java
    for (Long animalId : animalIds) {
        Animal animal = animalRepository.findById(animalId); // DB call per iteration
        results.add(animal);
    }
    ```

=== "Groovy"

    ```groovy
    animalIds.each { id ->
        def animal = animalRepository.findById(id) // DB call per iteration
        results.add(animal)
    }
    ```

## Good Example

```java
Map<Long, Animal> animalMap = animalRepository.findAllById(animalIds)
    .stream()
    .collect(Collectors.toMap(Animal::getId, Function.identity()));

for (Long animalId : animalIds) {
    Animal animal = animalMap.get(animalId); // O(1) map lookup
    results.add(animal);
}
```

## How Detection Works

1. Identifies loop constructs (for, while, forEach, each, stream operations)
2. Inside each loop, looks for calls matching repository fetch patterns:
    - Method names: `findById`, `getById`, `findOne`, `getOne`, `loadById`, `countBy*`
    - Compound patterns: `getAnimalByAnimalId`, `findUserByEmail`
3. Also checks target variable names for repository indicators (`repository`, `dao`, `store`, `service`, `client`)
4. Supports cross-method resolution to catch hidden repository calls in helper methods

## Suggestion

> Bulk fetch all needed records before the loop, or build an in-memory Map

## Related Rules

- [Bulk Load for Single Lookup](full-scan-for-single-lookup.md) — loading all records when only one is needed
