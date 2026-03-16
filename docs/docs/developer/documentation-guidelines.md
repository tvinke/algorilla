# Documentation Guidelines

Conventions for writing documentation pages, rule examples, and narrative deep dives.

## Tone

Direct, technical, slightly conversational. The reader is a developer — don't talk down, don't over-explain obvious things, but don't be dry either. A line like "good luck finding that in a stack trace" is fine. A paragraph of formal academic prose is not.

- **Be direct.** Lead with the point, not the setup.
- **Be honest about trade-offs.** If a fix makes code less readable, say so. If a pattern only matters at scale, say that too.
- **Acknowledge the reader's reality.** Enterprise codebases are messy. Fixes aren't always simple. Don't pretend `new HashSet<>()` solves everything when the collection lives behind three service boundaries. A short "in practice, this might require..." goes a long way.
- **Vary the register.** Not every sentence needs to be crisp and punchy. Mix short observations with longer explanations. Monotone writing — whether monotone-formal or monotone-casual — reads as robotic.

## Complexity notation

When showing complexity in **technical contexts** (rule metadata tables, evidence chains, code comments), use formal Big-O: `O(n²) → O(n)`, `O(n·IO) → O(1·IO + n)`.

When showing complexity in **summary or marketing contexts** (the "What it finds" table, taglines, landing page bullets, badge labels), use plain language that a developer can understand without mentally parsing notation. Compare:

- Formal: `k·O(f) → O(f)`
- Plain: `Same work done k times → done once`

The test: if someone scanning the page has to stop and decode the notation, it's too formal for that context. Rule detail pages and evidence chains are where readers expect precision. Overview tables and landing pages are where they expect quick understanding.

## Example domain

All code examples should use the [example domain](example-domain.md) (orders, payments, products, etc.). This keeps the documentation cohesive — readers build context once and carry it across pages.

## Rule page template

Every rule page follows this structure, in this order:

1. **Front matter** — tags (languages, frameworks, keywords) and `description` meta for search snippets
2. **Title** — rule name as `# heading`
3. **Rule details** — admonition table with Rule ID, Severity, Confidence, Category, Complexity
4. **"The problem"** — 2-3 paragraphs. Start with the one-liner, then real-world context: where this shows up, why it passes code review, what the production impact looks like
5. **"Where this shows up"** — 2-5 bullets describing real-world scenarios by framework/ecosystem. Narrative, not code.
6. **"The pattern"** — the anti-pattern with language tabs. Add framework-specific tabs only where the API is meaningfully different
7. **"The fix"** — expanded with multiple fix variants where applicable (e.g. HashSet approach AND Map approach, or findAllById AND custom @Query). Language tabs mirroring the pattern section
8. **"Suggestion variants"** — the `Suggestion` subtypes this rule emits, shown as content tabs with a mini code snippet per variant
9. **"When to ignore this"** — brief guidance on when suppression is legitimate. Builds trust.
10. **"How detection works"** — numbered list of what the rule engine does (keep for complex rules, drop for straightforward ones)
11. **"Related rules"** — narrative prose grouping related rules by cluster (I/O cluster, lookup cluster, loop overhead cluster, etc.), not just bare links
12. **"Configuration"** — 3-5 lines showing disable in `.algorilla.yml` + inline suppression

Not every rule page needs all sections — simpler rules can skip "How detection works" and "Suggestion variants" — but the order should be consistent. See [nested-lookup](../rules/nested-lookup.md) as the reference implementation.

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

## Framework-specific fixes

When a rule's fix section recommends a framework-specific API or concept (fetch joins, `@EntityGraph`, `Promise.all`, `useMemo`, etc.), **always link to the framework's own documentation**. The reader may have landed on this page directly from a console finding and may not know what the concept means.

- **Link the concept, not just the API name.** "Use a [JPQL fetch join](https://docs.oracle.com/...)" is better than just "Use a fetch join." The reader needs to know _where to learn more_, not just _what to type_.
- **Prefer official docs over blog posts.** Link to Spring Reference, MDN, Kotlin docs, Hibernate User Guide, etc.
- **One link per concept is enough.** Don't link every mention — link it the first time it appears in the fix section.
- **If the fix has multiple framework variants** (e.g. Spring Data vs plain JPA), show both with their own links.

This applies to rule pages, language pages, and the frameworks page. If we mention a framework concept as part of a fix, the reader should be one click away from understanding it.

## Output examples

When a page includes algorilla console output (the `⏺ file (N findings)` blocks), it must match the **current output format**. Stale output with old formatting undermines trust — the reader tries to match what they see in their terminal against the docs and it doesn't line up.

If the output format changes, update all output blocks across the docs. The pages that contain output examples:

- `index.md` (quick example)
- `hidden-complexity.md` (Step 2 output)
- `hidden-duplication.md` (triple scan output)
- `hidden-io.md` (N+1 output)
- `guide/understanding-output.md` (format reference — this one defines the canonical format)
- `getting-started/quickstart.md` (example output section)
- `languages/java.md`, `languages/kotlin.md`, `languages/groovy.md`, `languages/javascript.md` (hero snippets and "Top complexity traps" scenarios)
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

## Material for MkDocs features

We use several Material theme features beyond the basics. Know what's available so you reach for the right tool.

### Linked content tabs (`content.tabs.link`)

Tabs with the same label sync across the page. When a reader picks "Kotlin" in one code block, all other tabbed blocks on the page switch too. This is why consistent tab labels matter — always use `=== "Java"`, `=== "Kotlin"`, `=== "Groovy"`, `=== "JavaScript"` (exact same strings, capitalized the same way).

### Definition lists (`def_list`)

Use for terms with explanations — severity levels, confidence levels, language maturity tiers. Cleaner than bullet lists with bold labels, and renders well on mobile.

```markdown
HIGH
:   Structurally proven. Very few false positives.

MEDIUM
:   Likely correct, depends on context.
```

Don't overuse — a regular bullet list is fine when definitions aren't needed. Definition lists are best for glossary-style content where the term is the entry point.

### Highlighted text (`pymdownx.mark`)

`==highlighted==` renders as ==highlighted==. Use for key complexity expressions in narrative pages where the reader needs to spot the critical part quickly: `==O(n²)==`, `==O(1)==`, `==n × m==`.

Rules of thumb:

- Highlight the complexity notation that's the point of the paragraph, not every occurrence
- Don't highlight inside code blocks (it doesn't render there)
- Don't highlight in rule pages — those have enough visual structure already. Reserve for the deep-dive narratives and the Big-O primer

### Footnotes

For caveats and asides that would break reading flow if inline. A footnote keeps the narrative clean for a beginner while providing depth for the curious.

```markdown
The total comparisons are ==n × m==.[^1]

[^1]: The "O" stands for *order* of growth. See the [Big-O primer](../concepts/big-o-primer.md).
```

Good uses: Big-O notation explanations, mathematical background, "technically this is..." clarifications. Bad uses: anything the reader needs to understand the main point — that belongs in the text, not in a footnote.

### Social cards (`social` plugin)

Auto-generates Open Graph preview images for link sharing (Slack, GitHub, Twitter). Enabled in CI only (`CI=true` env var) because it needs native Cairo libraries. No content changes needed — it uses each page's title and description from front matter.

Pages with good `description` front matter get better social cards. Rule pages and language pages already have this.

### Navigation tracking

URL hash updates as the reader scrolls through sections. No content changes needed — it just works. This means readers can share deep-links to specific sections (e.g., `understanding-output/#confidence-levels`).

### Tooltips and glossary (`content.tooltips` + `abbr` + `snippets`)

Terms defined in `includes/glossary.md` get automatic tooltips on hover across all pages. Add new terms there when introducing jargon that appears on multiple pages.

## Cross-linking

Pages should link to related content, but consistently:

- **Rule pages** link to related rules at the bottom (under "Related Rules").
- **Deep-dive pages** link to the Big-O primer and rules overview at the bottom. They may also cross-reference each other where the patterns overlap (e.g. hidden-io references hidden-complexity's method indirection).
- **The index page** links to deep dives (under "Deep dives") and rule categories (under "What it finds").
- **The Big-O primer** links back to the hidden-complexity page and to the rules overview.

Don't over-link. A rule page doesn't need to link to every tangentially related concept. Link when a reader is likely to want more context on something mentioned in passing.
