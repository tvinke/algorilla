---
tags:
  - JavaScript
---

# JavaScript / TypeScript

!!! note "Beta"
    TypeScript and JavaScript analysis relies on name-based heuristics since these languages lack static type declarations. Expect higher false-positive rates than JVM languages, especially on large codebases. Use `--confidence high` to see only the most certain findings.

JavaScript arrays have a method called `includes()`. It does what you think — and it's O(n):

```javascript
const processed = getProcessedIds();

for (const event of incoming) {
    if (processed.includes(event.id)) {
        skip(event);
    }
}
```

Algorilla catches it:

```
warning  · nested-lookup · Loop amplifiers · O(incoming × processed) → O(incoming + processed)

  Linear includes on 'processed' inside for-each loop
  → Build a HashSet/Map from 'processed' before the loop

      3 │ for (const event of incoming) {
      4 │     if (processed.includes(event.id)) {

  ⎿  for-each loop over incoming   O(incoming)
    ⎿  includes on 'processed'   O(processed) ← bottleneck
```

The fix is `new Set(processed)` and `.has()` instead of `.includes()`. Whether that matters depends on the size of `processed` — but now you can make that call consciously.

## Regex recompilation in loops

This one surprises people. Regex literals inside loops get recompiled on every iteration:

```javascript
for (const line of lines) {
    const parts = line.replace(/\s+/g, ' ');
    const matched = line.match(/^(\d+)\s+(.*)$/);
}
```

```
warning  · implicit-regex-in-loop · O(|lines| × compile) → O(|lines|)

  replace() compiles a regex on every call inside for-each loop

  ⎿  for-each loop   O(|lines|)
    ⎿  line.replace() inside loop   compile ← bottleneck
```

Pull the regex out of the loop, assign it to a `const`, done.

## TypeScript type inference

When TypeScript type annotations are present, Algorilla uses them for more accurate analysis:

```typescript
const ids: Set<string> = new Set(rawIds);
items.filter(i => ids.has(i.id));  // Not flagged — Set.has is O(1)
```

Without the type annotation, a `new Set()` is still recognized. But explicit types help in ambiguous cases.

## Vue single-file components

The parser extracts `<script>` and `<script setup>` blocks from `.vue` files and analyzes the JavaScript/TypeScript within. Template expressions are not analyzed.

Supported extensions: `.js`, `.mjs`, `.cjs`, `.ts`, `.tsx`, `.vue`

## Rules not available

Two rules target JVM-specific APIs and don't apply to JavaScript/TypeScript:

- `repeated-reflection-in-loop` — Java reflection has no JS equivalent
- `parallel-pipeline-bottleneck` — parallel streams are a JVM concept

## See also

- [All rules](../rules/index.md) — 21 of 23 rules apply to JavaScript/TypeScript
- [Framework support](frameworks.md) — built-in knowledge of Vue, React, Angular, RxJS, Lodash, and Node.js for fewer false positives
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
