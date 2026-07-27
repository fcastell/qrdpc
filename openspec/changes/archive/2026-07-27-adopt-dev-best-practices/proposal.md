## Why

The sibling project `rms-mobile-app` (same toolchain: AGP 9.3.1, Kotlin 2.2.20, same
developer) has accumulated a set of working practices over several sessions — Claude Code
collaboration conventions, code quality tooling, CI, and a tag-based signed release
process — that QrDPC does not have yet. QrDPC is now published on GitHub (see the archived
`publish-to-github-fcastell` change) but still has no linting, no CI, and no reproducible
release process. Porting the proven parts of that setup now, while the codebase is still
small, is cheaper than retrofitting it later and keeps both projects consistent for their
shared maintainer. Everything here is adapted to QrDPC's context: single `:app` module (no
multi-module `core`/`feature` split), no dedicated target hardware, and all
documentation/commits in English (unlike `rms-mobile-app`, which is French — that
convention is explicitly NOT ported).

## What Changes

- Add a root `CLAUDE.md` documenting QrDPC-specific working conventions for Claude Code,
  pointing to the skills below rather than duplicating their content.
- Add a `git-pr-workflow` skill: dedicated branches, commit/PR/merge checklist, the rule
  that an OpenSpec change must be archived before its PR opens (with the CI-dependent-task
  exception), squash-merge + delete-branch as the default merge mode.
- Add a `session-wrapup` skill: end-of-session checklist (git state, OpenSpec state,
  recurring patterns worth formalizing, spec/code drift check).
- Add a `verify-on-device` skill, generalized from the Zebra-specific original: build,
  install, cold-start, and screenshot-verify UI changes on a real connected Android device
  (no specific hardware target for QrDPC, so no device-model restriction — but still no
  emulator-only verification for UI work).
- Add code quality tooling: `detekt` (static analysis) and `spotless`/`ktlint` (formatting),
  with a root `detekt.yml` (ignore `FunctionNaming` for `@Composable`) and `.editorconfig`
  entry, matching `rms-mobile-app`'s configuration.
- Add a `lefthook` pre-commit hook running `spotlessApply` on staged Kotlin files.
- Add a GitHub Actions CI workflow (`.github/workflows/ci.yml`): `spotlessCheck`, `detekt`,
  `test`, `lint`, `assemble`, triggered on push to `main` and on pull requests, with
  `paths-ignore` for `openspec/**`, `docs/**`, and `**/*.md`.
- Add a tag-triggered `release` CI job: builds a signed release APK from a
  `vX.Y.Z` git tag, renames it `qrdpc-vX.Y.Z.apk`, and publishes a GitHub Release with
  auto-generated notes (in English).
- Add a `release-app` skill documenting the tag/version-bump/publish procedure.
- Add a conditional release `signingConfig` to `app/build.gradle.kts`: active only when
  `ANDROID_KEYSTORE_BASE64` (and related secrets) are present in the environment, so local
  builds without those secrets remain unsigned and unaffected — mirrors
  `rms-mobile-app`'s approach exactly.
- **BREAKING (process, not code)**: once this change is archived, `git-pr-workflow` becomes
  the default expected flow for all future work on this repo — direct commits to `main`
  without a branch/PR are no longer the norm.

## Capabilities

### New Capabilities
- `claude-workflow-conventions`: CLAUDE.md plus the `git-pr-workflow` and `session-wrapup`
  skills that govern how Claude Code operates on this repo (branching, commits, PRs,
  OpenSpec archiving, session close-out).
- `ui-verification`: the `verify-on-device` skill and the expectation that UI changes are
  confirmed on a real device before being considered done.
- `code-quality-tooling`: detekt + spotless/ktlint configuration and the lefthook
  pre-commit hook that auto-formats staged Kotlin files.
- `ci-pipeline`: the GitHub Actions workflow that runs formatting/lint/static-analysis/test/
  build checks on every push to `main` and every pull request.
- `release-process`: the tag-triggered, signed-APK release CI job and the `release-app`
  skill describing how to cut a release.

### Modified Capabilities
(none — no existing requirement changes)

## Impact

- Affected paths: repo root (`CLAUDE.md`, `.editorconfig`, `detekt.yml`, `lefthook.yml`),
  `.github/workflows/ci.yml` (new), `app/build.gradle.kts` and `build.gradle.kts` (detekt/
  spotless plugin wiring, signing config), `gradle/libs.versions.toml` (new version entries
  for `detekt`, `spotless`, `ktlint`), `.claude/skills/` (three new skill files).
- Affected systems: GitHub Actions (new workflow, new repository secrets for release
  signing), GitHub repository settings (secrets: `ANDROID_KEYSTORE_BASE64`,
  `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`,
  `GRADLE_ENCRYPTION_KEY`).
- New local dependency: `lefthook` must be installed and its git hooks synced
  (`lefthook install`) for the pre-commit auto-format to run.
- A release signing keystore does not yet exist for QrDPC and must be generated as part of
  this change (see design.md) — this is a new, sensitive artifact that needs careful
  handling (not committed to the repo, stored only as a base64 GitHub secret).
- No impact on the app's existing runtime behavior.
