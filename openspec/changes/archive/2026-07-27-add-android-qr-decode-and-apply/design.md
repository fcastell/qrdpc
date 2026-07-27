## Context

`ScannerScreen` in `MainActivity.kt` currently only handles the camera permission
request; the CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`,
`camera-view`) and ML Kit Barcode Scanning (`mlkit-barcode-scanning`) dependencies are
already declared in `gradle/libs.versions.toml` but unused. On the other side of the
loop, the `qr-config-generator` web tool (archived change `add-qr-config-generator`)
already produces QR codes encoding a JSON payload per the `qr-payload-format` contract:
`{schemaVersion, packageName, restrictions: [{key, type, value}]}`, UTF-8-encoded and
prefixed with a BOM (`EF BB BF`) for cross-scanner compatibility. This change builds the
consuming side: scan → decode → validate → confirm → apply.

Testing this end to end requires a device where this app has been granted the
`APP_RESTRICTIONS` delegation scope by an active DPC (verified available: a Zebra
TC501L with TestDPC as profile owner is connected in this environment) — without that
grant, the apply step is expected to fail with a specific, user-visible error, which
is itself part of what needs to be verified.

## Goals / Non-Goals

**Goals:**
- Scan a QR code, decode and validate its payload against `qr-payload-format` exactly
  as documented (including the BOM-strip requirement), and surface a clear error for
  anything malformed rather than crashing or silently ignoring it.
- Never apply anything without an explicit confirmation screen showing what's about to
  change.
- Apply via the delegated `DevicePolicyManager.setApplicationRestrictions` call, with
  distinguishable, actionable error states (delegation missing vs. target app missing
  vs. other failure).

**Non-Goals:**
- No changes to the `qr-payload-format` contract itself — this change is a consumer of
  it, not a modifier.
- No support for multi-QR/paginated payloads (matches the generator's own non-goal).
- No persistence of scan history or previously-applied configs — each scan is a fresh,
  one-shot flow.
- No handling of `RestrictionsManager`-side "managed configuration changed" callbacks
  on the target app — QrDPC's job ends at calling `setApplicationRestrictions`;
  whether/how the target app reacts to the change is outside this app's control.

## Decisions

- **ML Kit Barcode Scanning, QR-only, via `ImageAnalysis`**: configure
  `BarcodeScannerOptions` with `setBarcodeFormats(Barcode.FORMAT_QR_CODE)` only (skip
  other formats for speed), feed it through a CameraX `ImageAnalysis.Analyzer` bound
  alongside a `Preview` use case rendered via a Compose `AndroidView` wrapping
  `PreviewView`. This is the standard, documented CameraX + ML Kit integration pattern.
- **Decode raw bytes ourselves, not `Barcode.rawValue`**: use `Barcode.rawBytes`
  (`ByteArray`) and decode as UTF-8 explicitly
  (`String(rawBytes, Charsets.UTF_8)`), then strip a leading `﻿` if present. Relying
  on ML Kit's own `rawValue` charset guessing would reintroduce exactly the kind of
  charset ambiguity the BOM convention was created to route around (see
  `qr-payload-format`'s "UTF-8 BOM prefix" requirement and the corruption this fixed on
  the generator side) — decoding explicitly makes this app's behavior independent of
  whatever heuristic ML Kit uses internally.
- **Parse with `org.json` (Android built-in), not a new serialization dependency**: the
  payload's `bundle`/`bundle_array` values are arbitrary nested JSON with types inferred
  from their native JSON shape, which doesn't map cleanly onto `kotlinx.serialization`'s
  static schemas without extra `JsonElement`-handling machinery anyway. `org.json`
  requires no new dependency and is a direct fit for "walk this JSON, validate shape,
  convert to Bundle."
- **Validation mirrors the generator's contract exactly**: `schemaVersion` (integer,
  must be `1` — the only version defined so far; a future different value is treated as
  unsupported, not guessed-at), `packageName` (non-empty string), `restrictions` (array;
  each entry has `key` (non-empty string), `type` (one of the seven known values), and a
  `value` whose JSON shape matches that type). Any violation aborts decoding with a
  specific message (not a generic "invalid QR") — this is the direct counterpart to the
  generator's own per-field validation.
- **Bundle construction**: top-level restriction types map directly
  (`bool`→`putBoolean`, `string`/`choice`→`putString`, `integer`→`putInt`,
  `multi-select`→`putStringArray`, `bundle`→`putBundle` with the nested object converted
  recursively, `bundle_array`→`putParcelableArray` of converted `Bundle`s). Inside a
  `bundle`/`bundle_array`, nested values are converted by native JSON type (object→
  nested `Bundle`, `JSONArray` of objects→`Bundle[]`, `JSONArray` of strings→
  `String[]`, boolean→`putBoolean`, string→`putString`, number→`putInt` if integral
  else rejected — `RESTRICTION_TYPE` has no float/double), matching the generator
  design's documented behavior exactly.
- **Confirmation screen is mandatory and shows real content**: package name plus a
  scrollable list of `key: type = value` (nested bundle/bundle_array shown as compact
  JSON), with explicit Apply and Rescan actions — never an implicit "scan = apply."
- **Apply via delegated call, package-existence checked first**: before calling
  `setApplicationRestrictions`, check `packageManager.getApplicationInfo(packageName,
  0)`; if that throws `NameNotFoundException`, show "target app not installed" without
  attempting the DPM call. Otherwise call
  `devicePolicyManager.setApplicationRestrictions(null, packageName, bundle)` (delegated
  form — `null` admin component); a `SecurityException` here specifically means the
  `APP_RESTRICTIONS` delegation scope hasn't been granted to this app for that DPC, and
  is shown as such (not a generic failure).
- **Scanning pauses on a hit, resumes on Rescan**: the `ImageAnalysis.Analyzer` stops
  processing frames once a valid-looking barcode is found (whether or not the payload
  turns out to validate), to avoid re-triggering mid-confirmation; "Rescan" (from either
  the confirmation or an error state) resumes analysis.
- **Root content uses `Modifier.safeDrawingPadding()`** — discovered during on-device
  testing: with targetSdk 36 drawing edge-to-edge by default, the confirmation screen's
  Apply/Rescan buttons rendered underneath the 3-button system navigation bar on the
  test device, and taps there were intercepted by the system UI instead of reaching the
  app (confirmed via `adb shell uiautomator dump`: the buttons' clickable bounds
  overlapped `navigationBarBackground`'s bounds). Applied once at the root `Surface` in
  `MainActivity` so every screen is covered without touching each one individually.
- **`QUERY_ALL_PACKAGES` permission, with a `tools:ignore` comment** — also discovered
  on-device: without it, `PackageManager.getApplicationInfo()` reported a
  genuinely-installed target package as not found, because Android 11+ package-visibility
  filtering hides other apps from a caller by default unless declared via `<queries>` —
  and QrDPC's target package is only known at scan time, so no static `<queries>` entry
  is possible. This is exactly the documented legitimate use case for the permission
  (device-management-style tools that must see arbitrary installed packages); the
  Android Lint `QueryAllPackagesPermission` warning is suppressed with a comment
  explaining why, rather than worked around.

## Risks / Trade-offs

- [`APP_RESTRICTIONS` delegation not granted] → Expected, common first-run state.
  Mitigation: explicit, distinct error message rather than a silent no-op or crash;
  covered by an explicit test scenario.
- [ML Kit `rawBytes` is null for some scanned codes (older ML Kit versions didn't always
  populate it for all formats)] → Fall back to `rawValue` (ML Kit's own decoded string)
  only if `rawBytes` is unavailable, still applying the BOM-strip step to whatever string
  is obtained.
- [Nested `bundle`/`bundle_array` values use a JSON type this conversion doesn't handle
  (e.g. a float)] → Rejected explicitly with a validation error rather than silently
  truncated/rounded, consistent with `RESTRICTION_TYPE` having no float/double
  equivalent.

## Migration Plan

1. Build the payload validation + `Bundle` conversion logic (plain Kotlin, unit-testable
   independent of CameraX/ML Kit).
2. Wire CameraX preview + ML Kit analyzer into `ScannerScreen`.
3. Build the confirmation screen and apply/error states.
4. Verify on the connected device: grant `APP_RESTRICTIONS` delegation to
   `io.github.fcastell.qrdpc` via TestDPC for a real installed target package, generate
   a QR with the web tool covering all 7 restriction types, scan it, confirm, apply, and
   verify the target app's restrictions changed (e.g. via
   `adb shell dumpsys device_policy` or the target app's own
   `RestrictionsManager.getApplicationRestrictions()` output).
5. Verify the delegation-not-granted and target-app-not-installed error paths
   explicitly (revoke delegation / scan a QR for a package that isn't installed).

## Open Questions

None outstanding — scope (full scan+parse+apply pipeline) confirmed by the user.
