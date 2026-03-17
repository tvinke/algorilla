# Understanding Output

## Console output format

Findings are grouped by file. Each file header shows the path and finding count:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (2 findings)
```

Each finding then shows:

```
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
      # a1b2c3d4e5f6g7h8  ← accept with --accept a1b2c3d4e5f6g7h8
```

The `← accept with --accept <hash>` hint appears on the first finding only; subsequent findings show just `# <hash>`.

Findings with HIGH confidence show a `✔ high confidence` marker after the severity tag. LOW confidence findings show `• low confidence`. MEDIUM (the default) has no marker. For example:

```
     warning ✔ high confidence · string-concat-in-loop · Loop amplifiers · O(n²) → O(n)
```
```
     warning • low confidence · lazy-loading-in-loop · Query patterns · O(n·IO) → O(1·IO + n)
```

The format breaks down as:

1. **Severity, confidence tag, rule ID, category, and complexity** on the first line
2. **Fully qualified location** (class name and line number)
3. **Description** of the detected pattern
4. **Suggestion** prefixed with `→`
5. **Rule URL** prefixed with `↳` — a clickable link to the rule's documentation page
6. **Code snippet** with line numbers showing the relevant context
7. **Evidence chain** with tree markers (`⎿`) tracing the path from outer context to the bottleneck
8. **Fingerprint hash** — a stable identifier for this finding, used with `--accept` to add it to the [ignore list](workflow.md#accept-it-ignore-list)

### Severity levels

Severity tells you how likely the finding is to be a real performance problem at production scale:

warning
:   Likely performance issue. These patterns typically cause measurable slowdowns once data volumes grow past test-sized inputs. Investigate these first.

info
:   Worth knowing about but may not matter in practice. The pattern exists, but the cost may be acceptable for your data sizes or the fix isn't straightforward.

!!! tip "Start with warnings"
    If algorilla reports many findings, focus on **warnings** first — they flag the patterns most likely to cause real performance issues. Use `--severity warning` to filter out info-level findings.

### Confidence levels

Confidence tells you how certain Algorilla is that the finding is correct — not how severe the problem is, but how much the tool trusts its own analysis:

HIGH
:   Structurally proven. The pattern is always an anti-pattern regardless of types (e.g. string concatenation in a loop), or type analysis confirms the data path. Very few false positives.

MEDIUM
:   Likely correct based on available evidence, but depends on context or type information that can't be fully verified statically. This is the default level.

LOW
:   Plausible but uncertain. Detection relies on naming conventions or heuristics. Worth investigating, but expect some false positives.

Use `--confidence high` for a focused view of the most trustworthy findings, or `--confidence low` to see everything.

### Complexity

The complexity notation on the first line (e.g. `O(orders × discountedProductIds) → O(orders + discountedProductIds)`) shows the cost before and after the suggested fix, using [Big-O notation](../concepts/big-o-primer.md). The arrow `→` means "could be reduced to." If you're unfamiliar with Big-O, the [Big-O primer](../concepts/big-o-primer.md) covers it in 5 minutes — the key insight is: O(n) means cost grows linearly with input, O(n²) means it grows with the _square_.

### Evidence chain

The evidence chain shows the execution path that leads to the finding. The `← bottleneck` annotation marks the innermost expensive operation. Indentation reflects nesting depth:

```
      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'discountedProductIds' OrderService.java:45 O(discountedProductIds) ← bottleneck
```

For cross-method findings, the chain shows the call path:

```
      ⎿  for-each loop over orders OrderService.java:31 O(orders)
        ⎿  hasDiscount() inside loop OrderService.java:33 O(discountedProductIds)
          ⎿  contains on 'discountedProductIds' DiscountService.java:12 O(discountedProductIds) ← bottleneck
```

### Finding count per evidence entry

When a call is repeated, the evidence shows the count:

```
      ⎿  getStringOrNull() (1st call) OrderView.java:29
      ⎿  getStringOrNull() (duplicate) OrderView.java:31
      ⎿  getStringOrNull() (duplicate) OrderView.java:33
```

### Overview section

When a scan produces more than 10 findings, an overview section appears before the individual findings. It shows severity counts, the top rules by frequency, and the hotspot file:

```
── Overview ────────────────────────────────
  82 info · 126 warning across 86 files
  Top rules: n-plus-one-query (41) · expensive-construction (27) · redundant-expensive-call (25)
  Hotspot: CategoryFacadeImpl.java (11 findings)
────────────────────────────────────────────

Tip: Focus on high-confidence findings with --confidence high
```

When a single info-level rule dominates the output (>30% of findings), a note suggests filtering:

```
  Note: redundant-expensive-call accounts for 428 findings — use --severity warning to focus on higher-impact issues
```

### Summary line

Every scan ends with a summary:

```
Scanned 695 files in 1.6s. Found 94 issues (13 warnings, 81 info) across 49 files.
```

## Multiple findings per file

Files with multiple findings show them sequentially under the same file header:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (3 findings)

     warning  · sort-for-last · Sort abuse · O(n log n) → O(n)
    com.example.shop.service.OrderService:67

      Sorting entire collection just to access the first element
      → Use .min(comparator) or .max(comparator) — O(n) single pass instead of O(n log n) sort — picks one element directly
      ↳ https://tvinke.github.io/algorilla/rules/sort-for-last

          65 │ return orders.stream()
          66 │         .sorted(Comparator.comparing(Order::getTotal).reversed())
          67 │         .findFirst();

      ⎿  sorts entire collection OrderService.java:66 O(n log n) ← bottleneck
        ⎿  then picks the first element — sort is wasted OrderService.java:67 O(1)

     info  · filter-after-sort · Sort abuse · O(n log n + k) → O(k log k + n)
    com.example.shop.service.OrderService:89

      filter() after sorted() — sorting all elements before filtering
      → Move filter() before sorted() to sort fewer elements
      ↳ https://tvinke.github.io/algorilla/rules/filter-after-sort

          87 │ return orders.stream()
          88 │         .sorted(Comparator.comparing(Order::getTotal).reversed())
          89 │         .filter(o -> "PAID".equals(o.getStatus()))
          90 │         .collect(Collectors.toList());

      ⎿  sorted call — sorts all N elements OrderService.java:88 O(n log n) ← bottleneck
        ⎿  filter after sort — discards elements after sorting OrderService.java:89 O(n)

     info  · redundant-expensive-call · Redundancy · O(2x) → O(1x)
    com.example.shop.service.OrderService:112

      calculateDiscount() called 2 times with same arguments in applyDiscounts()
      → Cache the result in a local variable
      ↳ https://tvinke.github.io/algorilla/rules/redundant-expensive-call

          110 │ if (calculateDiscount(order.getLineItems()).compareTo(BigDecimal.ZERO) > 0) {
              │
          112 │     applyDiscount(order, calculateDiscount(order.getLineItems()));
          113 │ }

      ⎿  calculateDiscount() (1st call) OrderService.java:110
      ⎿  calculateDiscount() (duplicate) OrderService.java:112
```

## SARIF output

SARIF (Static Analysis Results Interchange Format) output follows the SARIF 2.1.0 specification and integrates with:

- GitHub Code Scanning
- Visual Studio Code (SARIF Viewer extension)
- Azure DevOps

```bash
algorilla --format sarif --output results.sarif .
```

## JSON output

Machine-readable JSON output for custom tooling:

```bash
algorilla --format json --output results.json .
```

The JSON output includes metadata for versioning and tooling:

- **`schemaVersion`** — format version number (currently `1`). Bumped when the structure changes.
- **`algorillaVersion`** — the version of algorilla that produced the output (e.g. `"0.2.0"`).

If you consume JSON output programmatically, **ignore unknown fields** — new fields may be added in any release without bumping `schemaVersion`. See [Stability & Compatibility](../stability.md) for the full contract.

## Triaging a large scan

A first scan on a big codebase can easily return dozens of findings. Rather than tackling them all at once, work in layers:

!!! tip "Overwhelmed by the output?"
    Use `--limit 5` to cap the display at 5 findings. You still get the full count in the summary, but the output stays manageable. Exit codes reflect all findings regardless of the limit.

1. **Start at the default (high confidence)**: Algorilla defaults to `--confidence high`, showing only findings where it has strong evidence — type-confirmed collection lookups, unambiguous IO calls, structural patterns. These are your quick wins.

2. **Fix or accept each finding**: Either fix the code, or if the pattern is intentional, accept it with `--accept <hash>`. The hash after each finding (e.g. `# a1b2c3d4`) is a stable fingerprint.

3. **Lock in your progress**: Run `algorilla --save-baseline baseline.json .` to snapshot the current state. From here on, scans only surface *new* findings.

4. **Widen to medium confidence**: `algorilla --confidence medium .` shows findings that are likely real but lack full type confirmation. These need a bit more judgment but often point to real issues.

5. **Explore with low confidence**: `algorilla --confidence low .` shows everything, including heuristic-based findings. Useful for thorough audits.

This way you start with the most trustworthy findings and progressively expand — never staring at a wall of uncertain output.
