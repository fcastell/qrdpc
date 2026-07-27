# release-process Specification

## Purpose
Defines QrDPC's tag-triggered signed release process — how a `vX.Y.Z` tag turns into a
signed APK published as a GitHub Release — ported from `a-sibling-project` with a QrDPC-
specific signing keystore and English release notes.

## Requirements

### Requirement: Tag-triggered signed release build
Pushing a git tag matching `v*.*.*` SHALL trigger a CI job that builds a signed release
APK, using `versionName`/`versionCode` derived from the tag
(`versionCode = MAJOR*10000 + MINOR*100 + PATCH`), and SHALL only run after the standard
CI checks (`spotlessCheck`, `detekt`, `test`, `lint`, `assemble`) have passed.

#### Scenario: Release job depends on the standard CI job
- **WHEN** a tag `vX.Y.Z` is pushed
- **THEN** the release job does not start building until the standard CI job for that
  commit has succeeded

#### Scenario: Version is derived from the tag
- **WHEN** tag `v1.2.3` is pushed
- **THEN** the built APK has `versionName = "1.2.3"` and
  `versionCode = 1*10000 + 2*100 + 3 = 10203`

### Requirement: Conditional local-vs-CI signing
The release build type SHALL only apply a signing configuration when
`ANDROID_KEYSTORE_BASE64` is present in the environment; local builds without that
variable SHALL continue to produce an unsigned release APK with no other behavior change.

#### Scenario: Local release build stays unsigned
- **WHEN** `./gradlew assembleRelease` runs locally without `ANDROID_KEYSTORE_BASE64` set
- **THEN** the resulting APK is unsigned, exactly as before this capability was added

#### Scenario: CI release build is signed
- **WHEN** the release CI job runs with `ANDROID_KEYSTORE_BASE64` and the associated
  signing secrets set
- **THEN** the resulting APK is signed with that keystore

### Requirement: Published GitHub Release with renamed APK
On a successful signed build, the release job SHALL publish a GitHub Release for that tag
with English release notes (fixed short description plus auto-generated notes) and the APK
attached, renamed to include the app name and version.

#### Scenario: APK asset is renamed
- **WHEN** the release job publishes tag `v1.2.3`
- **THEN** the attached APK asset is named `qrdpc-v1.2.3.apk`

#### Scenario: Release notes are in English
- **WHEN** the GitHub Release is published
- **THEN** its notes are written in English
