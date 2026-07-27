## Why

QrDPC can scan a camera permission grant but nothing else: `MainActivity`'s scanner
screen is a placeholder ("Camera permission granted — scanner not wired up yet."), and
the web-based QR generator (`qr-config-generator`, already shipped) has no consumer.
This closes the loop the whole project exists for: scan a QR code produced by that
generator, decode it per the `qr-payload-format` contract, and apply the restrictions
to the target app via the `DELEGATION_APP_RESTRICTIONS` scope.

## What Changes

- Wire up CameraX preview + ML Kit Barcode Scanning (QR-only) in `ScannerScreen`,
  replacing the placeholder text.
- Decode a scanned QR's raw bytes as UTF-8, strip the leading BOM per
  `qr-payload-format`, and parse/validate the JSON against that same contract
  (`schemaVersion`, `packageName`, typed `restrictions`) — reject and surface a clear
  error for anything that doesn't conform, mirroring the validation the web generator
  already performs on the way in.
- Convert a valid payload's `restrictions` into an `android.os.Bundle` (typed
  top-level entries; nested `bundle`/`bundle_array` values inferred from their native
  JSON type, matching the generator's documented behavior).
- Show a confirmation screen before applying anything: target package name, the
  restrictions about to be applied, Apply / Rescan actions — scanning never applies
  silently.
- Apply the bundle via
  `DevicePolicyManager.setApplicationRestrictions(null, packageName, bundle)` (the
  delegated-app call form, per `DELEGATION_APP_RESTRICTIONS`), with explicit,
  distinguishable error states: delegation not granted, target package not installed,
  and any other failure — each with a clear message and a way to retry/rescan rather
  than a generic crash or silent no-op.
- **BREAKING (behavior, not code)**: this is QrDPC's first version that actually
  modifies another app's state on the device. Scanning a QR code is no longer a no-op
  demo — it requires explicit user confirmation before anything is applied, and it
  requires this app to already have been delegated `APP_RESTRICTIONS` by the active DPC
  for the target package, or the apply step fails with an explicit error.

## Capabilities

### New Capabilities
- `qr-scanning`: the CameraX + ML Kit Barcode Scanning camera UI that captures a QR
  code's raw bytes.
- `restrictions-application`: decoding a captured QR payload per `qr-payload-format`,
  validating it, converting it to a `Bundle`, showing a confirmation UI, and applying it
  via `DevicePolicyManager`'s delegated `setApplicationRestrictions` call, with explicit
  error handling.

### Modified Capabilities
(none — `qr-payload-format` already defines the contract this change consumes; no
requirement changes to it)

## Impact

- Affected paths: `app/src/main/kotlin/io/github/fcastell/qrdpc/MainActivity.kt`
  (replaced/split into scanning + confirmation + apply UI), `gradle/libs.versions.toml`
  / `app/build.gradle.kts` (no new dependencies expected — CameraX and ML Kit Barcode
  Scanning are already present; parsing uses Android's built-in `org.json`).
- Affected systems: the device's `DevicePolicyManager` / active DPC delegation state
  for whichever target package the user scans a QR for.
- Verification requires a real device with a DPC (e.g. TestDPC) that has granted this
  app the `APP_RESTRICTIONS` delegation scope for a target test package — see
  `verify-on-device`.
