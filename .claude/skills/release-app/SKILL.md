---
name: release-app
description: Creates a new tagged release of QrDPC (build + signature + APK publication via CI). Use when the user asks to create/publish/cut a new release or version of the app.
metadata:
  author: fabien-castell
  version: "1.0"
---

A QrDPC release is a simple `vX.Y.Z` git tag pushed to the remote: CI
(`.github/workflows/ci.yml`, `release` job, triggered only on a `v*.*.*` tag push) builds
the signed APK, renames it `qrdpc-vX.Y.Z.apk`, and publishes a GitHub Release with English
notes (fixed short text + `--generate-notes`). No branch or PR needed for a release: it's
a tag, not a change to `main`'s content.

## Steps

1. **Current state**
   - `git tag -l --sort=-v:refname | head -5` and `gh release list --limit 5` to see the
     latest tagged/published version.
   - `git status --short` and `git checkout main && git pull --ff-only` to confirm `main`
     is up to date and clean (a release tags the current `main` as-is; it doesn't wait for
     in-progress changes).

2. **Determine the version**
   - If the user gives an explicit version number, use it as-is.
   - Otherwise, ask for the bump type (patch / minor / major) via the question tool —
     don't infer it from commits alone, that choice stays with the user.
   - Compute `vX.Y.Z` from the last tag + SemVer bump (patch: +0.0.1; minor: +0.1.0,
     patch reset to 0; major: +1.0.0, minor and patch reset to 0).
   - Verify that tag doesn't already exist (`git tag -l vX.Y.Z`).

3. **Tag and push**
   ```bash
   git tag -a vX.Y.Z -m "Version X.Y.Z" main
   git push origin vX.Y.Z
   ```

4. **Follow CI through to publication**
   - The `release` job depends on the `android` job (`needs: android`) — both must
     succeed. Follow the run triggered by the tag push (`gh run list`/`gh run view`) until
     completion.
   - On CI failure: don't re-push the same tag (tags aren't silently rewritten) —
     diagnose, fix on `main` via the normal flow (`git-pr-workflow`), delete the existing
     tag (local + remote), then recreate it once the fix is merged.

5. **Confirm the published release**
   - `gh release view vX.Y.Z`: verify the `qrdpc-vX.Y.Z.apk` asset is present and the
     notes are in English.
   - Give the release URL to the user.

## Quick reference

- The release job (`ci.yml`) generates the fixed release-notes text itself — no need to
  draft it in this skill, unless the user explicitly asks for different content (in that
  case, edit `ci.yml` rather than publishing the release manually outside CI).
- Deleting an existing release/tag (e.g. version mistake): `gh release delete vX.Y.Z
  --yes` then `git tag -d vX.Y.Z && git push origin :refs/tags/vX.Y.Z` — always confirm
  with the user first, see `git-pr-workflow` for caution around hard-to-reverse actions.
