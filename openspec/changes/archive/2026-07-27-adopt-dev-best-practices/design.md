## Context

A sibling Android project (same maintainer, same core toolchain versions) has, over
several sessions, settled on: a documented Claude Code workflow (branch/PR discipline
tied to OpenSpec archiving), detekt + spotless/ktlint for code quality, a lefthook
pre-commit hook, a GitHub Actions CI pipeline, and a tag-triggered signed-release job.
QrDPC currently has none of this: no `.github/workflows/`, no linting or formatting
enforcement, no pre-commit hook, and no release process beyond manually building
`:app:installDebug` locally. This design ports the reusable parts, adapted to QrDPC's
differences from that sibling project:

- Single `:app` Gradle module (no `core`/`feature` module split), so tooling wiring is
  simpler — no per-module repetition needed.
- No dedicated target hardware (no fixed device model equivalent) — device verification
  stays device-agnostic.
- All docs/commits/release notes in English, not French — every piece of copied
  configuration or generated text is translated, not just the code.
- No existing signing keystore or GitHub secrets — these need to be created from scratch
  for QrDPC specifically, not reused from the sibling project (separate app, separate
  identity, separate blast radius if compromised).

## Goals / Non-Goals

**Goals:**
- Bring QrDPC's CI/tooling/workflow baseline to parity with the sibling project for the
  parts that generalize: formatting/lint/static-analysis/test/build on every PR, a
  documented Claude Code workflow, and a reproducible signed-release process.
- Keep local development frictionless: no secrets required to build/run locally; the
  release signing config activates only in CI, exactly like the sibling project.

**Non-Goals:**
- No multi-module restructuring — QrDPC stays a single `:app` module; detekt/spotless are
  wired directly in `app/build.gradle.kts` (plus `kotlinGradle` formatting at the root),
  not replicated per-module.
- No French-language conventions of any kind (commit messages, release notes, PR bodies) —
  QrDPC stays English-only throughout, per existing project convention.
- No tester installation guide — QrDPC has no internal tester distribution process yet;
  the release job's notes stay a short fixed English blurb plus `--generate-notes`, with
  no guide link, until/unless that need arises.
- No Play Store publishing — "release" here means a signed APK attached to a GitHub
  Release, matching the sibling project's current scope exactly.

## Decisions

- **Single-module tooling wiring**: apply the `detekt` and `spotless` plugins directly in
  `app/build.gradle.kts` (Kotlin source) and in the root `build.gradle.kts`
  (`kotlinGradle` target for `*.gradle.kts` files), reusing the same root `detekt.yml`
  (`FunctionNaming` ignored for `@Composable`) and `.editorconfig` entry as the sibling
  project. Versions (`detekt` 1.23.8, `spotless` 8.8.0, `ktlint` 1.8.0) copied into
  `gradle/libs.versions.toml` to match the already-shared AGP/Kotlin versions.
- **lefthook over a Gradle-only check**: keep the same pre-commit hook shape as the
  sibling project (`spotless-apply` running `./gradlew spotlessApply` with
  `stage_fixed: true`) rather than only relying on CI to catch formatting — catches issues
  before push, consistent with that project.
- **CI workflow structure mirrors the sibling project's `ci.yml`** (fail-fast ordering:
  `spotlessCheck` → `detekt` → `test` → `lint` → `assemble`), same pinned action versions
  (`actions/checkout@v7`, `actions/setup-java@v5`, `gradle/actions/setup-gradle` pinned to
  the same commit SHA as v6.2.0, `android-actions/setup-android` pinned to the same commit
  SHA as v4.0.1) for consistency and to reuse the already-vetted supply-chain choices.
  `paths-ignore` covers `openspec/**`, `docs/**`, `**/*.md`, same rationale as the sibling
  project (doc/spec-only changes don't need a full Android build).
- **New `GRADLE_ENCRYPTION_KEY` secret dedicated to QrDPC** (`openssl rand -base64 32`),
  not reused from the sibling project — each repo's Gradle configuration-cache encryption
  key stays independent.
- **New signing keystore dedicated to QrDPC**, generated locally with `keytool`
  specifically for this app (distinct identity from the sibling project's), stored only as
  a base64-encoded `ANDROID_KEYSTORE_BASE64` GitHub Actions secret (plus
  `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`) — the `.jks`
  file itself is never committed. `app/build.gradle.kts` gets the same conditional
  `signingConfig` pattern as the sibling project: present only when
  `ANDROID_KEYSTORE_BASE64` is set in the environment, so local builds without that secret
  produce an unsigned release APK exactly as today (no regression for local development).
- **`versionCode`/`versionName` sourced from Gradle properties** (`-PversionName`/
  `-PversionCode`), defaulting to the current hardcoded values (`1` / `"0.1.0"`) for local
  builds — same mechanism as the sibling project, so the release job can derive them from
  the git tag (`vX.Y.Z` → `versionCode = MAJOR*10000 + MINOR*100 + PATCH`).
- **Release notes in English**: fixed text along the lines of "QrDPC test build." plus
  `--generate-notes`, no installation-guide link (see Non-Goals). APK asset renamed
  `qrdpc-vX.Y.Z.apk`, matching the sibling project's APK naming convention translated to
  this app's name.
- **`verify-on-device` generalized**: keep the build → install → cold-start →
  screenshot → inspect loop and the "never conclude a UI task from `assemble`/`test`
  alone" rule, but drop the fixed-hardware constraint entirely — any connected, unlocked,
  debug-enabled Android device (`adb devices -l` non-empty) is acceptable for QrDPC, since
  it has no fixed target hardware.
- **`git-pr-workflow` and `session-wrapup` ported near-verbatim**, translated to English,
  with the source project's package/app references replaced by `qrdpc`/
  `io.github.fcastell.qrdpc`, and the French-specific commit-message/PR-body line replaced
  by "commit messages in English" (this repo's existing convention).

## Risks / Trade-offs

- [Release keystore is lost or its GitHub secrets are deleted] → Future releases can no
  longer update an already-installed QrDPC APK signed with the old key (Android requires
  matching signatures for updates). Mitigation: after generating the keystore, the user
  must back it up somewhere durable outside of GitHub (password manager, encrypted local
  storage) before this change is considered done — flagged as an explicit task, not
  automated by Claude Code.
- [`ANDROID_KEYSTORE_BASE64` accidentally unset/removed from GitHub secrets] → Release job
  silently produces an *unsigned* APK instead of failing loudly (same behavior as the
  sibling project, inherited intentionally for consistency). Mitigation: the
  `release-app` skill's step 5 explicitly checks the published release, and CI could
  additionally assert the APK is signed — left as a possible follow-up, not blocking this
  change.
- [Pinned third-party Action SHAs (`gradle/actions/setup-gradle`,
  `android-actions/setup-android`) go stale/unmaintained over time] → Same trade-off
  already accepted on the sibling project; mitigation is periodic manual review, not
  automated here.
- [Contributors who clone the repo don't run `lefthook install`] → Pre-commit formatting
  doesn't run locally for them, caught instead by CI's `spotlessCheck` on their PR.
  Mitigation: document `lefthook install` as a one-time setup step in `README.md`.

## Migration Plan

1. Add `.editorconfig` entry, `detekt.yml`, `lefthook.yml`, and wire `detekt`/`spotless`
   plugins + versions into the Gradle build.
2. Run `spotlessApply` once to reformat existing sources to the new `ktlint` rules before
   enabling `spotlessCheck` in CI, so CI starts green.
3. Add `.github/workflows/ci.yml` (build/lint job only first) and confirm it passes on a
   PR.
4. Generate the QrDPC release keystore locally, register the four signing secrets plus
   `GRADLE_ENCRYPTION_KEY` on the GitHub repo (`gh secret set`), back up the keystore file
   outside of git/GitHub.
5. Add the conditional `signingConfig` to `app/build.gradle.kts` and the `release` job to
   `ci.yml`.
6. Add the `CLAUDE.md`, `git-pr-workflow`, `session-wrapup`, `verify-on-device`, and
   `release-app` skill files.
7. Do a first end-to-end dry run: open a throwaway branch/PR to confirm the full CI job
   (`spotlessCheck`, `detekt`, `test`, `lint`, `assemble`) passes, then tag a `v0.1.0`
   test release to confirm the signed-release job works end to end.

No rollback beyond reverting the added files/workflow and removing the GitHub secrets if
something is fundamentally wrong — nothing else depends on this yet.

## Open Questions

None outstanding — scope confirmed as "everything, including signed release" by the user.
