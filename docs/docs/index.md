# Algorilla

**Detect algorithmic complexity anti-patterns in your codebase.**

Algorilla is a static analysis tool that finds hidden O(n²) and O(n·m) performance issues in [Java](languages/java.md), [Kotlin](languages/kotlin.md), [Groovy](languages/groovy.md), and [JavaScript/TypeScript](languages/javascript.md) code. It analyzes your source code without executing it, identifying patterns where a simple refactoring can turn quadratic behavior into linear.

<div class="grid cards" markdown>

-   :material-rocket-launch:{ .lg .middle } **Getting Started**

    Install algorilla, run your first scan, and configure rules for your project.

    [:octicons-arrow-right-24: Quick start](getting-started/quickstart.md)

-   :material-shield-search:{ .lg .middle } **Rules**

    Browse all 28 built-in rules with examples, detection logic, and suggested fixes.

    [:octicons-arrow-right-24: Rules overview](rules/index.md)

-   :material-translate:{ .lg .middle } **Languages**

    Java (GA), Kotlin (Alpha), Groovy (Beta), JavaScript/TypeScript (Beta) — parser details, framework knowledge, and rule coverage per language.

    [:octicons-arrow-right-24: Language support](languages/index.md)

-   :material-console:{ .lg .middle } **Guide**

    CLI reference, CI/CD integration, baseline workflows, custom rules, and more.

    [:octicons-arrow-right-24: User guide](guide/cli-reference.md)

-   :material-book-open-variant:{ .lg .middle } **Learn**

    Why performance patterns hide in clean code, how Big-O works, and how algorilla detects them.

    [:octicons-arrow-right-24: The hidden O(n²) problem](hidden-complexity.md)

-   :material-code-braces:{ .lg .middle } **Developer**

    Architecture, coding conventions, and how to add new rules or language parsers.

    [:octicons-arrow-right-24: Developer docs](developer/architecture.md)

-   :material-file-document-edit:{ .lg .middle } **Changelog**

    What's new in each release.

    [:octicons-arrow-right-24: Changelog](changelog.md)

-   :material-shield-check:{ .lg .middle } **Stability**

    What's stable, what's experimental, and how we evolve without breaking you.

    [:octicons-arrow-right-24: Stability & Compatibility](stability.md)

-   :material-frequently-asked-questions:{ .lg .middle } **FAQ**

    Common questions about confidence, types, suppression, comparisons with other tools, and more.

    [:octicons-arrow-right-24: Frequently asked questions](faq.md)

</div>

## Quick example

Point algorilla at your project:

```bash
algorilla /path/to/your/project
# or: algorilla --input /path/to/your/project
```

Algorilla scans your source files and reports what it finds. Here it detected a `List.contains()` call inside a for-each loop — meaning every iteration does a linear scan of `discountedProductIds`, making the whole method O(n²). Not sure why that's a problem? Read [the hidden O(n²) problem](hidden-complexity.md) first.

```
⏺ src/main/java/com/example/shop/service/OrderService.java (1 finding)

     warning  · nested-lookup · Loop amplifiers · O(orders × discountedProductIds) → O(orders + discountedProductIds)
    com.example.shop.service.OrderService:45

      Linear contains on 'discountedProductIds' inside for-each loop
      → Build a HashSet/Map from 'discountedProductIds' before the loop
      ↳ https://tvinke.github.io/algorilla/rules/nested-lookup

          38 │ public List<Order> applyDiscounts(List<Order> orders, List<String> discountedProductIds) {
             │
          44 │ for (Order order : orders) {
          45 │     if (discountedProductIds.contains(order.getProductId())) {
          46 │         matched.add(order);

      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'discountedProductIds' OrderService.java:45 O(discountedProductIds) ← bottleneck

Scanned 127 files in 0.4s. Found 1 issue (1 warning) across 1 file.
```

??? tip "Reading the output"

    Reading from top to bottom: the **severity**, **rule name**, **category**, and **complexity trade-off** are on the first line. Below that, a **description** explains the problem in plain language, followed by a concrete **suggestion** and a **link to the rule's docs page**. The **code snippet** shows the exact lines involved, and the **evidence chain** (the `⎿` tree at the bottom) traces the path from outer loop to bottleneck. The fix here: convert `discountedProductIds` to a `HashSet` before the loop for O(1) lookups. See [understanding output](guide/understanding-output.md) for the full format reference.

## What it finds

| Category | Examples | What it catches |
|----------|----------|----------------|
| [Loop amplifiers](rules/index.md#loop-amplifiers) | Nested lookup, repeated regex, IO in loop, ... | Expensive work inside loops that multiplies cost per iteration |
| [Sort abuse](rules/index.md#sort-abuse) | Sort for last, expensive comparator, filter after sort | Sorting entire collections when a simpler operation would do |
| [Query patterns](rules/index.md#query-patterns) | N+1 query, bulk load for single lookup | Database/service calls that could be batched into one |
| [Construction cost](rules/index.md#construction-cost) | Heavyweight object per invocation | Expensive objects created repeatedly instead of reused |
| [Redundancy](rules/index.md#redundancy) | Redundant calls, uncached getters, chained getters | Same work done multiple times when once would suffice |

## Deep dives

These pages walk through how specific patterns hide in real code, step by step:

- [**The hidden O(n²) problem**](hidden-complexity.md) — how `List.contains()` inside a loop creates quadratic behavior that hides behind method calls and layers of indirection
- [**The hidden triple scan**](hidden-duplication.md) — how clean, readable stream pipelines quietly traverse the same collection multiple times
- [**The hidden N+1**](hidden-io.md) — how a simple `findById()` in a loop turns into thousands of database round-trips that only surface in production

## Why algorilla?

Most static analyzers focus on correctness — null checks, type errors, security flaws. Algorilla focuses on **algorithmic cost**: the patterns that look fine in tests with small data but become expensive at production volumes. A `List.contains()` inside a loop looks harmless until the list has 10,000 entries.

What it offers:

- **28 built-in rules** across 6 categories, each with evidence chains that show *why* it's a problem
- **Multi-language**: [Java](languages/java.md), [Kotlin](languages/kotlin.md), [Groovy](languages/groovy.md), [JavaScript/TypeScript](languages/javascript.md) (including `.vue` single-file components and JSX)
- **Cross-file analysis**: follows call chains across files and languages to catch indirect patterns
- **Fast**: analyzes 500+ files/second with incremental caching
- **Zero config**: works out of the box with automatic project detection

## New to Algorilla?

If you're evaluating the tool or just want to understand what it does, this reading order gets you there quickly:

1. **[The hidden O(n²) problem](hidden-complexity.md)** — see how performance bugs hide in clean code (10 min read)
2. **[Quick start](getting-started/quickstart.md)** — install and run your first scan (2 min)
3. **[Pick your language](languages/index.md)** — Java, Kotlin, Groovy, or JS/TS — see framework-specific examples
4. **[Browse the rules](rules/index.md)** — 28 rules with plain-language impact descriptions

Already installed? Jump to the [workflow guide](guide/workflow.md) or [add it to CI](guide/ci-integration.md).

## Next steps

<div class="grid cards" markdown>

-   **Try it on your project** — [Installation](getting-started/installation.md) takes under a minute. Run it, see what it finds.

-   **Browse the rules** — [28 rules](rules/index.md) with before/after examples so you know exactly what to look for.

-   **Add it to CI** — [CI/CD integration](guide/ci-integration.md) with GitHub Actions, GitLab CI, and baseline workflows.

-   **Understand the output** — [Evidence chains](guide/understanding-output.md) explain every finding step by step.

</div>
