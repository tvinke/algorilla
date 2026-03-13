---
tags:
  - Groovy
---

# Groovy

!!! note "Beta"
    Groovy analysis is usable with caveats. Some false positives expected around GDK-specific patterns. Use `--confidence high` for best results.

Groovy closures make iteration feel lightweight. That's the trap:

```groovy
def activeIds = orderRepository.getActiveOrderIds()

items.each { item ->
    if (activeIds.contains(item.id)) {
        process(item)
    }
}
```

`activeIds` is a `List` — `contains()` is O(n), called once per item inside the closure. Algorilla catches it:

```
warning  · nested-lookup · Loop amplifiers · O(items × activeIds) → O(items + activeIds)

  Linear contains on 'activeIds' inside forEach()
  → Build a HashSet/Map from 'activeIds' before the loop

      4 │     if (activeIds.contains(item.id)) {

  ⎿  forEach() over items   O(items)
    ⎿  contains on 'activeIds'   O(activeIds) ← bottleneck
```

For a dozen items, nobody notices. For batch processing with thousands, the difference between `List` and `Set` is the difference between seconds and milliseconds.

## Closures and GDK methods

Algorilla treats Groovy closures as lambda equivalents. GDK methods like `collect`, `find`, `findAll`, `each`, `eachWithIndex`, `inject`, and `groupBy` are recognized as iteration boundaries — the same way `stream().filter()` is in Java.

```groovy
// Flagged: expensive-callback
items.findAll { LocalDate.parse(it.dateStr).isAfter(cutoff) }
```

```groovy
// Flagged: sort-for-last via GDK
def oldest = people.sort { it.age }.last()

// Better:
def oldest = people.max { it.age }
```

## Dynamic typing

Groovy is dynamically typed, so collection types often can't be inferred statically. When Algorilla reports false positives on `contains()` calls against Sets or Maps, use [type hints](../getting-started/configuration.md):

```yaml
type-hints:
  idSet: HashSet
  lookupMap: HashMap
```

## See also

- [All rules](../rules/index.md) — the full set of patterns Algorilla detects, all 23 apply to Groovy
- [Framework support](frameworks.md) — built-in knowledge of Grails/GORM and Spock for fewer false positives
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
