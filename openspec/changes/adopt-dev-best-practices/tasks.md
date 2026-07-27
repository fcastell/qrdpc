## 1. Code quality tooling

- [x] 1.1 Add `detekt`, `spotless`, `ktlint` versions and plugin aliases to
      `gradle/libs.versions.toml`
- [x] 1.2 Add root `detekt.yml` (`FunctionNaming` ignored for `@Composable`)
- [x] 1.3 Add `.editorconfig` entry for `ktlint_function_naming_ignore_when_annotated_with`
- [x] 1.4 Wire `detekt` + `spotless` (kotlin + kotlinGradle targets) into
      `app/build.gradle.kts`; wire `spotless` (kotlinGradle target) into root
      `build.gradle.kts`
- [x] 1.5 Run `./gradlew spotlessApply` once to reformat existing sources, then verify
      `./gradlew spotlessCheck detekt` both pass clean (also rephrased a pre-existing TODO
      comment in `MainActivity.kt` that tripped detekt's default `ForbiddenComment` rule)

## 2. Pre-commit hook

- [x] 2.1 Add `lefthook.yml` with a `pre-commit` `spotless-apply` command
      (`./gradlew spotlessApply`, `stage_fixed: true`) for `*.{kt,kts}`
- [x] 2.2 Install lefthook locally (`lefthook install`) and verify the hook runs on a test
      commit (verified via `lefthook run pre-commit` against a throwaway misformatted file,
      confirmed reformat + re-stage, then removed the throwaway file — no real commit made)
- [x] 2.3 Document the one-time `lefthook install` setup step in `README.md`

## 3. CI pipeline

- [x] 3.1 Add `.github/workflows/ci.yml` with the `android` job: checkout, setup-java 17,
      setup-gradle (pinned SHA, cache-encryption-key), setup-android (pinned SHA), accept
      SDK licenses, install SDK components, then `spotlessCheck` → `detekt` → `test` →
      `lint` → `assemble`
- [x] 3.2 Set trigger on push to `main` (and tags `v*.*.*`) and on pull requests to `main`,
      with `paths-ignore` for `openspec/**`, `docs/**`, `**/*.md`
- [x] 3.3 Generate a `GRADLE_ENCRYPTION_KEY` (`openssl rand -base64 32`) and register it as
      a GitHub Actions secret on `fcastell/qrdpc`
- [x] 3.4 Open a throwaway branch/PR and confirm the CI job passes end to end (verified on
      this change's real PR, https://github.com/fcastell/qrdpc/pull/1, rather than a
      separate disposable one — `android` job green in 4m25s, `release` correctly skipped)

## 4. Release signing and process

- [x] 4.1 Generate a new release keystore dedicated to QrDPC locally with `keytool`
      (PKCS12, RSA 2048, alias `qrdpc`, valid until 2053 — note: PKCS12 keystores use a
      single password for both store and key)
- [x] 4.2 Register `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`,
      `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` as GitHub Actions secrets on
      `fcastell/qrdpc`
- [x] 4.3 Back up the keystore file itself somewhere durable outside git/GitHub (confirmed
      by the user; ephemeral scratchpad copy removed afterward)
- [x] 4.4 Update `app/build.gradle.kts`: conditional `signingConfig` (active only when
      `ANDROID_KEYSTORE_BASE64` is set), `versionCode`/`versionName` sourced from Gradle
      properties with current values as fallback
- [x] 4.5 Add the `release` job to `ci.yml`: `needs: android`, triggered only on
      `v*.*.*` tag pushes, computes version from the tag, builds `assembleRelease` with the
      signing secrets, renames the APK to `qrdpc-vX.Y.Z.apk`, publishes a GitHub Release
      with English notes (`--generate-notes`, no installation-guide link) and the APK
      attached
- [ ] 4.6 Tag a `v0.1.0` test release and confirm the signed APK is built and the GitHub
      Release is published correctly

## 5. Claude Code workflow conventions

- [x] 5.1 Add `.claude/skills/git-pr-workflow/SKILL.md`, translated/adapted from
      `a-sibling-project` (English, `qrdpc`/`io.github.fcastell.qrdpc` references, commit
      messages in English instead of French)
- [x] 5.2 Add `.claude/skills/session-wrapup/SKILL.md`, translated/adapted similarly
- [x] 5.3 Add `.claude/skills/verify-on-device/SKILL.md`, generalized: no Zebra/Sunmi
      hardware constraint, any connected debug-enabled Android device accepted
- [x] 5.4 Add `.claude/skills/release-app/SKILL.md`, adapted to QrDPC's tag/version/release
      flow and English release notes
- [x] 5.5 Add root `CLAUDE.md` referencing these four skills (git/PR/archive workflow, CI
      notes, release procedure, device verification, session wrap-up) without duplicating
      their content
