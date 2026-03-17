---
tags:
  - Groovy
  - Spock
  - performance
description: "Groovy complexity analysis with Algorilla — detects GORM dynamic finder N+1 queries, GDK collection traps, hidden iteration in closures, and more. 28 rules apply."
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
  ↗ https://tvinke.github.io/algorilla/rules/nested-lookup

      4 │     if (activeIds.contains(item.id)) {

  ⎿  forEach() over items   O(items)
    ⎿  contains on 'activeIds'   O(activeIds) ← bottleneck
```

For a dozen items, nobody notices. For batch processing with thousands, the difference between `List` and `Set` is the difference between seconds and milliseconds.

## Top complexity traps in Groovy

### GORM dynamic finders in .each {}

The classic N+1 in Grails. GORM's dynamic finders (`findById`, `findByName`, `countByStatus`) each hit the database. Inside `.each {}`, that's one query per item.

```groovy
orderIds.each { id ->
    def order = Order.findById(id) // DB round-trip per iteration
    results.add(transform(order))
}
```

```
warning  · n-plus-one-query · Query patterns · O(orderIds·IO) → O(1·IO + orderIds)

  findById() called inside forEach() — N+1 query pattern
  → Bulk fetch all needed records before the loop, or build an in-memory Map
  ↗ https://tvinke.github.io/algorilla/rules/n-plus-one-query
```

Fix: [`Order.findAllByIdInList(orderIds)`](https://gorm.grails.org/latest/hibernate/manual/index.html#simpleQueries) or a criteria query. See [n-plus-one-query](../rules/n-plus-one-query.md).

### GDK collection methods and hidden iteration

Groovy's `findAll`, `collect`, `find` are iteration boundaries. When they contain a linear lookup, you get the same O(n²) as a nested `for` loop — but it doesn't _look_ like nesting.

```groovy
def blocklist = loadBlockedIds() // ArrayList

def allowed = requests.findAll { req ->
    !blocklist.contains(req.sourceId) // O(n) per request
}
```

```
warning  · nested-lookup · O(requests × blocklist) → O(requests + blocklist)

  → Build a HashSet/Map from 'blocklist' before the loop
  ↗ https://tvinke.github.io/algorilla/rules/nested-lookup
```

Fix: `def blockSet = blocklist.toSet()`. See [nested-lookup](../rules/nested-lookup.md).

### .collect { ... } with an inner loop

A `collect` that calls a method containing its own loop. The outer iteration is visible, but the inner one is hidden behind the method call.

```groovy
def summaries = departments.collect { dept ->
    summarize(dept) // hides a loop over dept.employees
}

def summarize(dept) {
    dept.employees.each { emp ->
        calculateBonus(emp)
    }
}
```

```
warning  · hidden-nested-loop · O(departments × dept.employees)

  summarize() contains a forEach() — hidden O(n²) complexity
  ↗ https://tvinke.github.io/algorilla/rules/hidden-nested-loop
```

Fix: flatten and batch, or accept the cost if the data volumes are small. See [hidden-nested-loop](../rules/hidden-nested-loop.md).

### sort { } then .last()

GDK's `sort` with a closure sorts the entire collection just to grab one element. `max` does the same job in O(n).

```groovy
def oldest = people.sort { it.age }.last()
```

```
warning  · sort-for-last · Sort abuse · O(n log n) → O(n)

  → Use max { it.age } — avoids sorting the entire collection
  ↗ https://tvinke.github.io/algorilla/rules/sort-for-last
```

See [sort-for-last](../rules/sort-for-last.md).

## Closures and GDK methods

Algorilla treats Groovy closures as lambda equivalents. GDK methods like `collect`, `find`, `findAll`, `each`, `eachWithIndex`, `inject`, and `groupBy` are recognized as iteration boundaries — the same way `stream().filter()` is in Java.

```groovy
// Flagged: expensive-callback
items.findAll { LocalDate.parse(it.dateStr).isAfter(cutoff) }
```

## Dynamic typing

Groovy is dynamically typed, so collection types often can't be inferred statically. When Algorilla reports false positives on `contains()` calls against Sets or Maps, use [type hints](../getting-started/configuration.md):

```yaml
type-hints:
  idSet: HashSet
  lookupMap: HashMap
```

## Framework knowledge

| Framework | What Algorilla knows | Most relevant rules |
|-----------|---------------------|-------------------|
| **Grails / GORM** | CRUD (`save`, `delete`, `get`), dynamic finders (`findBy*`, `countBy*`), criteria/where queries, controller methods | [n-plus-one-query](../rules/n-plus-one-query.md), [io-in-loop](../rules/io-in-loop.md) |
| **Spock** | Mock creation, interaction DSL, mocking operators | — (test framework, mainly false-positive suppression) |

Groovy also inherits all Java framework knowledge — Spring, JPA, Guava apply when detected in Groovy code.

## See also

- [All rules](../rules/index.md) — the full set of patterns Algorilla detects, all 28 apply to Groovy
- [Framework support](frameworks.md) — built-in knowledge of Grails/GORM and Spock
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
