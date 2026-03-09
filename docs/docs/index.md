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

```bash
java -jar algorilla.jar /path/to/your/project
```

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × eligibleIds) → O(orders + eligibleIds)
    com.example.shop.service.OrderService:45

      Linear contains on 'eligibleIds' inside for-each loop
      → Build a HashSet/Map from 'eligibleIds' before the loop

          38 │ public List<Order> findEligibleOrders(List<Order> orders, List<String> eligibleIds) {
             │
          44 │ for (Order order : orders) {
          45 │     if (eligibleIds.contains(order.getId())) {
          46 │         matched.add(order);

      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'eligibleIds' OrderService.java:45 O(eligibleIds) ← bottleneck

Scanned 127 files in 0.4s. Found 1 issues (0 errors, 1 warnings) across 1 files.
```

## What it finds

| Category | Rules | Typical impact |
|----------|-------|----------------|
| [Loop amplifiers](rules/index.md#loop-amplifiers) | Nested lookup, repeated regex, serialization in loop, ... | O(n²) → O(n) |
| [Sort abuse](rules/index.md#sort-abuse) | Sort for last, expensive comparator, filter after sort | O(n² log n) → O(n log n) |
| [Query patterns](rules/index.md#query-patterns) | N+1 query, bulk load for single lookup | O(n·IO) → O(IO) |
| [Construction cost](rules/index.md#construction-cost) | Heavyweight object per invocation | Allocation overhead |
| [Redundancy](rules/index.md#redundancy) | Redundant calls, uncached getters, chained getters | k·O(f) → O(f) |

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
