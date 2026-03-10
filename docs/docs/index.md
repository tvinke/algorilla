# Algorilla

**Detect algorithmic complexity anti-patterns in your codebase.**

Algorilla is a static analysis tool that finds hidden O(n²) and O(n·m) performance issues in Java, Groovy, Kotlin, and JavaScript/TypeScript code. It analyzes your source code without executing it, identifying patterns where a simple refactoring can turn quadratic behavior into linear.

<div class="grid cards" markdown>

-   :material-rocket-launch:{ .lg .middle } **Getting Started**

    Install algorilla, run your first scan, and configure rules for your project.

    [:octicons-arrow-right-24: Quick start](getting-started/quickstart.md)

-   :material-shield-search:{ .lg .middle } **Rules**

    Browse all 15 built-in rules with examples, detection logic, and suggested fixes.

    [:octicons-arrow-right-24: Rules overview](rules/index.md)

-   :material-console:{ .lg .middle } **Guide**

    CLI reference, CI/CD integration, baseline workflows, custom rules, and more.

    [:octicons-arrow-right-24: User guide](guide/cli-reference.md)

-   :material-book-open-variant:{ .lg .middle } **Concepts**

    Understand how algorilla detects patterns, builds call graphs, and analyzes across files.

    [:octicons-arrow-right-24: How detection works](concepts/how-detection-works.md)

-   :material-code-braces:{ .lg .middle } **Developer**

    Architecture, coding conventions, and how to add new rules or language parsers.

    [:octicons-arrow-right-24: Developer docs](developer/architecture.md)

-   :material-file-document-edit:{ .lg .middle } **Changelog**

    What's new in each release.

    [:octicons-arrow-right-24: Changelog](changelog.md)

</div>

## Quick example

Point algorilla at your project:

```bash
java -jar algorilla.jar /path/to/your/project
```

Algorilla scans your source files and reports what it finds. Here it detected a `List.contains()` call inside a for-each loop — meaning every iteration does a linear scan of `discountedProductIds`, making the whole method O(n²). Not sure why that's a problem? Read [the hidden O(n²) problem](hidden-complexity.md) first.

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × discountedProductIds) → O(orders + discountedProductIds)
    com.example.shop.service.OrderService:45

      Linear contains on 'discountedProductIds' inside for-each loop
      → Build a HashSet/Map from 'discountedProductIds' before the loop

          38 │ public List<Order> applyDiscounts(List<Order> orders, List<String> discountedProductIds) {
             │
          44 │ for (Order order : orders) {
          45 │     if (discountedProductIds.contains(order.getProductId())) {
          46 │         matched.add(order);

      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'discountedProductIds' OrderService.java:45 O(discountedProductIds) ← bottleneck

Scanned 127 files in 0.4s. Found 1 issues (0 errors, 1 warnings) across 1 files.
```

??? tip "Reading the output"

    Reading from top to bottom: the **severity**, **rule name**, **category**, and **complexity trade-off** are on the first line. Below that, a **description** explains the problem in plain language, followed by a concrete **suggestion**. The **code snippet** shows the exact lines involved, and the **evidence chain** (the `⎿` tree at the bottom) traces the path from outer loop to bottleneck. The fix here: convert `discountedProductIds` to a `HashSet` before the loop for O(1) lookups. See [understanding output](guide/understanding-output.md) for the full format reference.

## What it finds

| Category | Rules | Typical impact |
|----------|-------|----------------|
| [Loop amplifiers](rules/index.md#loop-amplifiers) | Nested lookup, repeated regex, serialization in loop, ... | O(n²) → O(n) |
| [Sort abuse](rules/index.md#sort-abuse) | Sort for last, expensive comparator, filter after sort | O(n² log n) → O(n log n) |
| [Query patterns](rules/index.md#query-patterns) | N+1 query, bulk load for single lookup | O(n·IO) → O(IO) |
| [Construction cost](rules/index.md#construction-cost) | Heavyweight object per invocation | Allocation overhead |
| [Redundancy](rules/index.md#redundancy) | Redundant calls, uncached getters, chained getters | k·O(f) → O(f) |

## Deep dives

These pages walk through how specific patterns hide in real code, step by step:

- [**The hidden O(n²) problem**](hidden-complexity.md) — how `List.contains()` inside a loop creates quadratic behavior that hides behind method calls and layers of indirection
- [**The hidden triple scan**](hidden-duplication.md) — how clean, readable stream pipelines quietly traverse the same collection multiple times
- [**The hidden N+1**](hidden-io.md) — how a simple `findById()` in a loop turns into thousands of database round-trips that only surface in production

## Why algorilla?

Most static analyzers focus on correctness — null checks, type errors, security flaws. Algorilla focuses on **algorithmic cost**: the patterns that pass code review, work fine in tests, and then buckle under production data volumes. A `List.contains()` inside a loop looks harmless until the list has 10,000 entries.

Algorilla catches these patterns **before** they ship:

- **15 built-in rules** across 5 categories, each with evidence chains that show *why* it's a problem
- **Multi-language**: Java, Groovy, Kotlin, JavaScript, TypeScript, Vue SFC
- **Cross-file analysis**: follows call chains across files and languages to catch indirect patterns
- **Fast**: analyzes thousands of files in seconds with incremental caching
- **Zero config**: works out of the box with automatic project detection

## Next steps

<div class="grid cards" markdown>

-   **Try it on your project** — [Installation](getting-started/installation.md) takes under a minute. Run it, see what it finds.

-   **Browse the rules** — [15 rules](rules/index.md) with bad/good examples so you know exactly what to look for.

-   **Add it to CI** — [CI/CD integration](guide/ci-integration.md) with GitHub Actions, GitLab CI, and baseline workflows.

-   **Understand the output** — [Evidence chains](guide/understanding-output.md) explain every finding step by step.

</div>
