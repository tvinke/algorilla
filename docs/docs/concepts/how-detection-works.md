# How Detection Works

Algorilla uses a multi-pass static analysis pipeline to detect algorithmic complexity anti-patterns.

## The Four-Pass Pipeline

### Pass 1: Parse & Collect

Each source file is parsed into a language-agnostic **Intermediate Representation (IR)**. The IR captures the structural elements relevant to complexity analysis:

- **LoopNode** — for, while, forEach, stream operations, Groovy `.each{}`
- **LookupCall** — contains, indexOf, find, filter, any, includes
- **SortCall** — sort, sorted, sortBy, Collections.sort
- **FunctionDecl** — method declarations with parameters
- **FunctionCall** — method invocations
- **ObjectCreation** — `new` expressions
- **VariableDecl** — local variable declarations with types
- **CollectionAccess** — findAll, getAll, list operations

### Pass 2: Call Graph Construction

Function declarations are registered in a project-wide **Symbol Table**. A **Call Graph** is built by resolving function calls to their declarations using qualified names and heuristic matching.

### Pass 3: Complexity Annotation

The call graph is traversed bottom-up to propagate execution context labels. This allows detection of patterns that span multiple methods (e.g., a lookup helper called from inside a loop).

### Pass 4: Rule Evaluation

Each rule traverses the annotated IR trees and reports findings. Rules have access to the full analysis context: IR trees, symbol table, call graph, and configuration.

## O(1) Type Detection

To avoid false positives, Algorilla checks whether a collection target is an O(1) data structure:

1. **Expression text** — checks for `Set`, `Map`, `HashMap`, `HashSet`, `TreeSet`, `TreeMap` in the target expression
2. **Parameter types** — if the target variable is a method parameter, checks its declared type
3. **Variable declarations** — checks the declared type of local variables
4. **Type hints** — user-provided hints in `.algorilla.yml`

## Confidence Tiers

Each finding carries a confidence level that tells you how certain Algorilla is that it's a real issue:

- **HIGH** — Structurally proven. The pattern is always an anti-pattern regardless of types (e.g. string concatenation in a loop), or parameter-flow analysis confirms the data path.
- **MEDIUM** — Likely correct based on available evidence, but depends on context or type information that Algorilla can't fully verify.
- **LOW** — Plausible but uncertain. Detection relies on naming conventions or heuristics. Worth investigating but expect some false positives.

By default, `--confidence medium` hides LOW findings. Use `--confidence high` for a focused view of the most trustworthy findings, or `--confidence low` to see everything.

Confidence is orthogonal to severity: a finding can be high-severity (impactful pattern) but low-confidence (uncertain detection), or vice versa.

## Overlap Resolution

Some rules look at the same code from different angles. For example, `list.filter(x -> ids.contains(x.id))` triggers both the `nested-lookup` rule (linear search inside a loop) and the `expensive-callback` rule (expensive operation inside a callback). Reporting both would be noisy.

Algorilla handles this automatically: when a more specific rule and a more general rule both fire on the same line, the more general finding is dropped. You always see the most precise diagnosis. If you suppress the specific finding (via `// algorilla:ignore`), the general one stays suppressed too — you won't see a "new" finding pop up after suppression.

## Suppression Handling

After overlap resolution, the **Suppression Filter** removes findings that have `// algorilla:ignore` comments on or near the finding's location and evidence chain locations.
