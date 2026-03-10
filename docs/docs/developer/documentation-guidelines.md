# Documentation Guidelines

Conventions for writing documentation pages, rule examples, and narrative deep dives.

## Tone

Direct, technical, slightly conversational. The reader is a developer — don't talk down, don't over-explain obvious things, but don't be dry either. A line like "good luck finding that in a stack trace" is fine. A paragraph of formal academic prose is not.

- **Be direct.** Lead with the point, not the setup.
- **Be honest about trade-offs.** If a fix makes code less readable, say so. If a pattern only matters at scale, say that too.
- **Acknowledge the reader's reality.** Enterprise codebases are messy. Fixes aren't always simple. Don't pretend `new HashSet<>()` solves everything when the collection lives behind three service boundaries. A short "in practice, this might require..." goes a long way.
- **Vary the register.** Not every sentence needs to be crisp and punchy. Mix short observations with longer explanations. Monotone writing — whether monotone-formal or monotone-casual — reads as robotic.

## Example domain

All code examples should use the [example domain](example-domain.md) (orders, payments, products, etc.). This keeps the documentation cohesive — readers build context once and carry it across pages.

## Rule page template

Every rule page follows this structure, in this order:

1. **Title** — rule name as `# heading`
2. **Rule ID line** — `` **Rule ID:** `rule-id` · **Severity:** WARNING · **Complexity:** O(bad) → O(good) ``
3. **Description** — what the rule detects and why it matters
4. **Bad Example** — the anti-pattern, with language tabs if applicable
5. **Good Example** — the fix, with language tabs if applicable
6. **How Detection Works** — numbered list of what the rule engine does (optional for simple rules)
7. **Suggestion** — the one-liner shown in algorilla output, as a blockquote
8. **Related Rules** — links to rules that detect similar or adjacent patterns (optional)

Not every rule page needs all sections — simpler rules can skip "How Detection Works" — but the order should be consistent.

### When to use language tabs

Use `=== "Java"` / `=== "Groovy"` / `=== "JavaScript"` tabs when the bad example looks meaningfully different across languages (different API names, different idioms). Don't add tabs just for the sake of completeness — if the Groovy version is identical to the Java version minus semicolons, a single Java example is clearer.

The good example doesn't always need tabs. If the fix concept is the same across languages, one language is enough.

## Deep-dive page template

The narrative pages (hidden-complexity, hidden-duplication, hidden-io) follow a common arc:

1. **Open with the core insight** — one paragraph that frames the problem
2. **Start simple** — the most obvious, recognizable version of the pattern
3. **Escalate step by step** — each section makes the pattern harder to spot (more indirection, more files, more realistic code)
4. **"What algorilla does about this"** — show the tool's output on one of the examples
5. **The fix** — before/after with full context, complexity comments
6. **Real-world nuance** — acknowledge that applying the fix in a real codebase may not be trivial
7. **Links** — Big-O primer and rules overview at the bottom

The escalation is what makes these pages work. Each step should feel like a natural evolution that a real codebase goes through over time — not a contrived setup.

## Writing bad/good examples

Every rule page has a "Bad Example" and "Good Example". The fix should be **visually obvious** — a reader scanning the two snippets should immediately see what changed structurally, without needing to understand the underlying type system.

- **Show the full context.** Don't show the fix in isolation — include the loop, the method, or whatever structure makes the problem visible. A `contains()` call on its own looks the same whether it's on a List or a HashSet.
- **Make the structural change jump out.** The good example should look different at a glance: a new line before the loop, a different API call (`sorted` → `max`), a variable extracted, a method call moved outside. If the only difference is a type name buried in a declaration, the payoff is invisible.
- **Comment the complexity, not the syntax.** Use `// O(1) per iteration` or `// O(n) per comparison` rather than `// Uses HashSet`. The reader should see *why* it's better, not just *what* changed.

## Output examples

When a page includes algorilla console output (the `⏺ file (N findings)` blocks), it must match the **current output format**. Stale output with old formatting undermines trust — the reader tries to match what they see in their terminal against the docs and it doesn't line up.

If the output format changes, update all output blocks across the docs. The pages that contain output examples:

- `index.md` (quick example)
- `hidden-complexity.md` (Step 2 output)
- `hidden-duplication.md` (triple scan output)
- `hidden-io.md` (N+1 output)
- `guide/understanding-output.md` (format reference — this one defines the canonical format)
- `README.md` (project README)

## Admonition blocks

Admonitions (`!!! warning`, `!!! tip`, etc.) work best as brief interruptions — a sidebar that adds context without derailing the narrative. If a page is mostly admonitions, they stop being interruptions and become the content, which defeats the purpose.

### When to use which type

- **`warning` / `danger`** — "this is worse than it looks." The reader might underestimate the problem; the admonition stops them. Use sparingly — if everything is dangerous, nothing is.

    !!! danger "This is the pattern that actually ships to production"
        Not the textbook nested loop — the real-world version where complexity is distributed across modules.

- **`tip`** — practical advice that's useful but tangential. The page should make sense if the reader skips it.

    !!! tip "You don't always need to combine everything"
        If you have two scans on a small collection, the constant factor is small and the stream version is more maintainable.

- **`info`** — supporting context ("why tests don't catch this", "how comparators are called"). Explains *why* something matters without being the main point.

    !!! info "Why tests don't catch this"
        Unit tests typically use small datasets — 3 orders, 2 discount rules. At that scale, O(n³) completes in microseconds.

- **`abstract`** — concept definitions for readers who need background. Use collapsible (`???`) if it's only relevant to some readers.

    ??? abstract "What is Big-O?"
        Big-O notation is a shorthand for "how does the cost of this code grow as the data grows?"

### When not to use them

- Don't put the main point inside an admonition. If removing it would leave a gap in the narrative, it belongs in the regular text.
- Don't stack multiple admonitions back-to-back. Two in a row is fine occasionally; three signals that the content should be restructured.
- Don't use them for code examples that are central to the page — code blocks in the main flow are easier to scan.

## Cross-linking

Pages should link to related content, but consistently:

- **Rule pages** link to related rules at the bottom (under "Related Rules").
- **Deep-dive pages** link to the Big-O primer and rules overview at the bottom. They may also cross-reference each other where the patterns overlap (e.g. hidden-io references hidden-complexity's method indirection).
- **The index page** links to deep dives (under "Deep dives") and rule categories (under "What it finds").
- **The Big-O primer** links back to the hidden-complexity page and to the rules overview.

Don't over-link. A rule page doesn't need to link to every tangentially related concept. Link when a reader is likely to want more context on something mentioned in passing.
