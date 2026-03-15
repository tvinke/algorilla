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

          38 │ public List<Order> applyDiscounts(List<Order> orders, List<String> discountedProductIds) {
             │
          44 │ for (Order order : orders) {
          45 │     if (discountedProductIds.contains(order.getProductId())) {
          46 │         matched.add(order);

      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'discountedProductIds' OrderService.java:45 O(discountedProductIds) ← bottleneck
      # a1b2c3d4e5f6g7h8
```

Findings with HIGH confidence show a `✔ high confidence` marker after the severity tag. LOW confidence findings show `• low confidence`. MEDIUM (the default) has no marker.

The format breaks down as:

1. **Severity, confidence tag, rule ID, category, and complexity** on the first line
2. **Fully qualified location** (class name and line number)
3. **Description** of the detected pattern
4. **Suggestion** prefixed with `→`
5. **Code snippet** with line numbers showing the relevant context
6. **Evidence chain** with tree markers (`⎿`) tracing the path from outer context to the bottleneck
7. **Fingerprint hash** — a stable identifier for this finding, used with `--accept` to add it to the [ignore list](workflow.md#accept-it-ignore-list)

### Severity levels

- **warning** — likely performance problems worth investigating
- **info** — informational findings that may or may not matter in practice

!!! tip "Start with warnings"
    If algorilla reports many findings, focus on **warnings** first — they flag the patterns most likely to cause real performance issues. Use `--severity warning` to filter out info-level findings.

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

      Sorting entire collection just to access a single element
      → Use .max(Comparator.comparing(...)) or .min(...) instead of sorting

          65 │ return orders.stream()
          66 │         .sorted(Comparator.comparing(Order::getTotal).reversed())
          67 │         .findFirst();

      ⎿  sorted call OrderService.java:66 O(n log n) ← bottleneck
        ⎿  .findFirst after sort OrderService.java:67 O(1)

     info  · filter-after-sort · Sort abuse · O(n log n + k) → O(k log k + n)
    com.example.shop.service.OrderService:89

      filter() after sorted() — sorting all elements before filtering
      → Move filter() before sorted() to sort fewer elements

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

1. **Start with high confidence**: `algorilla scan src/ --confidence high` limits output to findings where algorilla is most certain. These are your quick wins — clear patterns that almost always indicate a real problem.

2. **Fix or accept each finding**: Either fix the code, or if the pattern is intentional, accept it: `algorilla accept a1b2c3d4e5f6g7h8`. That hash after each finding (e.g. `# a1b2c3d4e5f6g7h8`) is a stable fingerprint — it won't change unless the finding itself changes.

3. **Lock in your progress**: Once you've worked through the batch, run `algorilla scan src/ --save-baseline` to snapshot the current state. From here on, scans only surface *new* findings.

4. **Widen to medium confidence**: Drop to `--confidence medium` (the default) and repeat. These findings need a bit more judgment but often point to real issues too.

This way you're never staring at a wall of output — just a manageable batch at a time.
