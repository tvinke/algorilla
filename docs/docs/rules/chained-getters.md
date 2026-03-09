# Chained Getters

**Rule ID:** `chained-getters` · **Severity:** INFO · **Complexity:** O(n^k) → O(n)

## Description

Detects cascading getter patterns where the result of one lookup feeds into another. If each getter performs a linear scan, the chain multiplies to O(n^k) where k is the chain length.

## Bad Example

```java
public String getOwnerName(Long registrationId) {
    Registration reg = findRegistration(registrationId);  // O(n)
    Animal animal = findAnimal(reg.getAnimalId());         // O(n), depends on result above
    Owner owner = findOwner(animal.getOwnerId());          // O(n), depends on result above
    return owner.getName();
}
```

## Good Example

```java
// Pre-build lookup maps
Map<Long, Registration> regMap = buildRegistrationMap();
Map<Long, Animal> animalMap = buildAnimalMap();
Map<Long, Owner> ownerMap = buildOwnerMap();

public String getOwnerName(Long registrationId) {
    Registration reg = regMap.get(registrationId);     // O(1)
    Animal animal = animalMap.get(reg.getAnimalId());  // O(1)
    Owner owner = ownerMap.get(animal.getOwnerId());   // O(1)
    return owner.getName();
}
```

## How Detection Works

1. Identifies variables initialized by getter-style calls (`get*`, `find*`, `load*`, `fetch*`)
2. Builds chains where a getter's argument references a variable produced by another getter
3. Flags chains of length 2 or more
4. Validates that at least one getter in the chain involves linear lookups

## Suggestion

> Pre-build lookup maps or use a join query to avoid cascading lookups

## Related Rules

- [Uncached Getter](uncached-getter.md) — same getter called multiple times with the same argument
- [N+1 Query](n-plus-one-query.md) — single-record fetch inside a loop
