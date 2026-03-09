# Pre-Commit Hooks

Run algorilla automatically before each commit to catch complexity anti-patterns early.

## Git Hook Setup

Create `.git/hooks/pre-commit` in your repository:

```bash
#!/usr/bin/env bash

# Get staged files (added, copied, modified)
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM)

if [ -z "$STAGED_FILES" ]; then
  exit 0
fi

echo "algorilla: scanning staged files..."

# Pass only staged files — no need for full project scan
echo "$STAGED_FILES" | xargs java -jar algorilla.jar --no-cache

if [ $? -ne 0 ]; then
  echo ""
  echo "algorilla found issues. Fix them or commit with --no-verify to skip."
  exit 1
fi
```

Make it executable:

```bash
chmod +x .git/hooks/pre-commit
```

## pre-commit Framework

If you use the [pre-commit](https://pre-commit.com) framework, add this to your `.pre-commit-config.yaml`:

```yaml
repos:
  - repo: https://github.com/tvinke/algorilla
    rev: v0.1.1
    hooks:
      - id: algorilla
        name: algorilla
        entry: java -jar algorilla.jar --no-cache
        language: system
        pass_filenames: true
        types_or: [java, groovy, kotlin, javascript, ts, vue]
```

For reference, the hook definition in `.pre-commit-hooks.yaml`:

```yaml
- id: algorilla
  name: algorilla
  description: Detect algorithmic complexity anti-patterns
  entry: java -jar algorilla.jar --no-cache
  language: system
  pass_filenames: true
  types_or: [java, groovy, kotlin, javascript, ts, vue]
```

## Tips

**Block only on warnings or above.** By default algorilla reports at all severity levels. To let informational findings pass without blocking the commit:

```bash
java -jar algorilla.jar --no-cache --severity warning "$@"
```

**Limit by language.** If your repo is mixed but you only want to scan Java files on commit:

```bash
java -jar algorilla.jar --no-cache --language java "$@"
```

**Keep it fast.** The hook scripts above pass only staged filenames to algorilla instead of scanning the whole project. Combined with `--no-cache` (which skips cache reads/writes), this keeps hook execution under a second for typical commits.
