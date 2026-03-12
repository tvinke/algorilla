# Releasing

This page describes how releases work and how to cut one manually if needed.

## How it works

Releases are automated via [Release Please](https://github.com/googleapis/release-please). The process:

1. You merge PRs with [conventional commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, etc.)
2. Release Please accumulates changes and opens a "release PR" that bumps the version in `gradle.properties` and updates `CHANGELOG.md`
3. You review and merge the release PR
4. Release Please creates a git tag (`v0.2.0`)
5. The tag triggers the release workflow, which builds and publishes to all channels

Your only manual step: **merge the release PR**.

## What gets published

The release workflow (`release.yml`) runs 4 parallel jobs after the build:

| Channel | Artifact | Secrets needed |
|---------|----------|----------------|
| GitHub Release | Shadow JAR (`algorilla-<version>.jar`) | (uses `GITHUB_TOKEN`) |
| npm | `algorilla` package on npmjs.org | `NPM_TOKEN` |
| Gradle Plugin Portal | `io.github.tvinke.algorilla` plugin | `GRADLE_PUBLISH_KEY`, `GRADLE_PUBLISH_SECRET` |
| Docker | `ghcr.io/tvinke/algorilla` image | (uses `GITHUB_TOKEN`) |

All secrets are repository secrets in GitHub (Settings > Secrets > Actions).

## Version management

The project version lives in one place: `gradle.properties`.

```properties
version=0.3.0-SNAPSHOT
```

All modules read this at build time. The CLI `--version` flag reads it from a generated properties file in the JAR. There is no hardcoded version string anywhere in source code.

**SNAPSHOT lifecycle:** during development, the version is always `X.Y.0-SNAPSHOT`. The release workflow strips `-SNAPSHOT` when building from a tag. After publishing, a post-release job automatically bumps to the next SNAPSHOT (e.g. `0.3.0` → `0.4.0-SNAPSHOT`).

Release Please manages the CHANGELOG and git tags, but does not touch `gradle.properties` — the release workflow handles that.

## Manual release

If you need to release without Release Please (e.g., first release, hotfix):

### 1. Update the version

Set `gradle.properties` to the clean release version (strip `-SNAPSHOT`):

```properties
version=0.3.0
```

Also update `.release-please-manifest.json` to match:

```json
{
  ".": "0.3.0"
}
```

### 2. Update the changelog

Edit `CHANGELOG.md` and replace the `(unreleased)` marker with the release date.

### 3. Commit, tag, push

```bash
git add gradle.properties .release-please-manifest.json CHANGELOG.md
git commit -m "chore: release 0.3.0"
git tag v0.3.0
git push origin main --tags
```

The tag triggers the release workflow.

### 4. Verify all channels

Check the [Actions tab](https://github.com/tvinke/algorilla/actions/workflows/release.yml) — all 4 publish jobs should succeed. See Troubleshooting below for common failures.

## Version scheme

Algorilla follows [Semantic Versioning](https://semver.org/):

- **Patch** (0.1.x): Bug fixes, documentation, minor rule tuning
- **Minor** (0.x.0): New rules, new CLI options, new language support
- **Major** (x.0.0): Breaking changes to config format, rule IDs, or output format

During the 0.x phase, minor versions may include breaking changes as the API stabilizes.

## Troubleshooting

### Release Please creates tags that don't trigger workflows

Tags created by Release Please using the default `GITHUB_TOKEN` don't trigger other workflows — this is a GitHub security restriction. Workarounds:

- Use a Personal Access Token (PAT) for Release Please instead of `GITHUB_TOKEN`
- Or manually delete and re-push the tag: `git tag -d v0.2.0 && git push --delete origin v0.2.0 && git tag v0.2.0 && git push origin v0.2.0`

### Gradle Plugin Portal rejects `com.github` group ID

The Portal requires `io.github` as the prefix for GitHub-based plugins. The plugin ID is `io.github.tvinke.algorilla` — this is permanent once published and cannot be changed later.

### npm publish fails with "cannot publish over previously published version"

npm doesn't allow re-publishing the same version. If the version was already published by a previous (partially failed) run, this error is harmless. To re-publish, you'd need to bump the version.

### npm publish fails with 403/auth error

Check that the `NPM_TOKEN` secret is set and the token has publish permissions for the `algorilla` package. Generate a new token at npmjs.org > Access Tokens > Granular Access Token.

### Docker push permission denied

The workflow uses `GITHUB_TOKEN` with `packages: write` permission. Ensure the workflow has this permission in the `permissions` block.

### Re-triggering a failed release

If some publish jobs fail but others succeed:

1. Fix the issue (code, secrets, or config)
2. Commit and push the fix
3. Move the tag to the new commit:
   ```bash
   git tag -f v0.2.0
   git push origin v0.2.0 --force
   ```
4. This triggers a fresh workflow run. Jobs that already published will fail with "already exists" errors — those are harmless.

npm and Docker publishes are idempotent — re-runs skip gracefully if the version already exists. Gradle Plugin Portal does not support re-publishing the same version.

**Caution:** `gh run rerun --failed` re-runs with the *original* commit, not the latest. Always move the tag instead.

### After a manual release: bump to SNAPSHOT

After tagging, bump `gradle.properties` to the next SNAPSHOT so local builds are clearly development versions:

```bash
# After releasing 0.3.0:
sed -i '' 's/^version=.*/version=0.4.0-SNAPSHOT/' gradle.properties
git add gradle.properties
git commit -m "chore: bump to 0.4.0-SNAPSHOT"
git push origin main
```

When using the automated release workflow, this happens automatically via the `bump-snapshot` job.
