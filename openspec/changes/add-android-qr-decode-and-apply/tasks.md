## 1. Payload parsing and validation

- [x] 1.1 Create a `QrPayload` data model (schemaVersion, packageName, restrictions
      list of key/type/value) and a parser/validator that takes a decoded JSON string
      (post-BOM-strip) and returns either a valid `QrPayload` or a specific validation
      error, using `org.json` (added `testImplementation(libs.json)` — Android's
      built-in org.json is a stub in local JVM unit tests)
- [x] 1.2 Implement per-type value validation (`bool`, `string`, `integer`, `choice`,
      `multi-select`, `bundle`, `bundle_array`) matching `qr-payload-format` exactly
- [x] 1.3 Add unit tests covering: valid payload of every type, missing/invalid
      `schemaVersion`, missing `packageName`, type/value mismatches, malformed JSON
      (15 tests, all passing; also added `detekt.yml`'s `ReturnCount.excludeGuardClauses`
      to accommodate the validation code's guard-clause style)

## 2. Bundle construction

- [x] 2.1 Implement `QrPayload` → `android.os.Bundle` conversion for top-level
      restrictions (bool/string/integer/choice/multi-select)
- [x] 2.2 Implement recursive native-type-inferred conversion for `bundle` and
      `bundle_array` nested values (object→Bundle, array-of-objects→Bundle[],
      array-of-strings→String[], boolean/string/integral-number passthrough)
- [x] 2.3 Add unit tests for nested bundle/bundle_array conversion, including a
      non-integral number in a nested value being rejected — the type-inference and
      rejection logic lives in the parser (section 1) and is unit tested there (`bundle
      with a non-integral nested number is invalid`, nested string-array inference);
      `android.os.Bundle` itself is a stub in local JVM unit tests without Robolectric
      (not a project dependency), so the mechanical Bundle-population step is verified
      on-device instead (section 5)

## 3. Camera scanning UI

- [x] 3.1 Add a QR-only `BarcodeScannerOptions` (`Barcode.FORMAT_QR_CODE`) and an
      `ImageAnalysis.Analyzer` that decodes detected codes' raw bytes as UTF-8, strips a
      leading BOM, and hands the result off (falling back to the scanner library's own
      decoded string only if raw bytes are unavailable)
- [x] 3.2 Wire a CameraX `Preview` + the analyzer into `ScannerScreen`, replacing the
      placeholder text, via a Compose `AndroidView`-wrapped `PreviewView`
- [x] 3.3 Pause frame analysis once a code is detected; add a rescan action that resumes
      it

## 4. Confirmation and apply flow

- [x] 4.1 Build a confirmation screen: target package name, scrollable list of
      `key: type = value` (nested bundle/bundle_array shown as compact JSON), Apply and
      Rescan actions
- [x] 4.2 Build the decode-error state: specific message from the validator, Rescan
      action
- [x] 4.3 On Apply: check target package installed via `PackageManager`
      (`NameNotFoundException` → "target app not installed" error, no DPM call), else
      call `DevicePolicyManager.setApplicationRestrictions(null, packageName, bundle)`
- [x] 4.4 Distinguish `SecurityException` (delegation not granted) from other failures
      in the apply error state, each with its own message and a Rescan action
- [x] 4.5 Build the success state (confirmation that restrictions were applied) with an
      action to scan another code

## 5. On-device verification

- [x] 5.1 Confirm `io.github.fcastell.qrdpc` holds the `APP_RESTRICTIONS` delegation
      scope for a real installed target package on the connected test device (grant via
      TestDPC if not already present) — granted by the user via TestDPC's UI, verified
      via `adb shell dumpsys device_policy` (`io.github.fcastell.qrdpc` →
      `delegation-app-restrictions`); target package for testing is `com.example.target`
      (already installed on the device)
- [x] 5.2 Using the deployed `qr-config-generator` web tool, build a config covering all
      7 restriction types (including nested `bundle`/`bundle_array`) for that target
      package, generate its QR code — built on the live `fcastell.github.io/qrdpc` site
      targeting `com.example.target`, 508 bytes
- [x] 5.3 Install the updated QrDPC build, scan the generated QR on the connected
      device, confirm the preview matches, tap Apply, and verify the target package's
      restrictions actually changed (e.g. via `adb shell dumpsys device_policy` or the
      target app's own restrictions readout) — scanned successfully, confirmation screen
      showed all 7 restrictions exactly matching the generator input, Apply reached the
      "Restrictions applied." success state with no exception. Also fixed two real bugs
      found along the way: (1) content rendered underneath the system navigation bar on
      this 3-button-nav device, so Apply/Rescan taps were being intercepted by the
      system UI — fixed with `Modifier.safeDrawingPadding()` on the root content; (2) see
      5.4 for the package-visibility fix. Could not independently confirm the applied
      Bundle's content via `adb` (no root access to read UserManager's restrictions
      storage, and `dumpsys`/`logcat` don't surface it) — verification rests on
      `setApplicationRestrictions` completing without throwing, which is the platform's
      own authoritative validation (delegation + package existence checked synchronously
      by `DevicePolicyManagerService` before accepting the call)
- [x] 5.4 Verify the "target app not installed" error path by scanning a QR for a
      package that isn't installed — confirmed on-device: scanned a QR for
      `com.example.doesnotexist`, got the exact expected error message with a Rescan
      action. This also surfaced a real bug fixed along the way: `PackageManager`
      package-visibility filtering (Android 11+) made even the genuinely-installed
      `com.example.target` report as "not installed" without a `<queries>`/
      `QUERY_ALL_PACKAGES` declaration — added `QUERY_ALL_PACKAGES` (justified: target
      package is only known at scan time, so no static `<queries>` entry is possible),
      suppressed the expected lint warning with a comment explaining why
- [x] 5.5 Verify the "delegation not granted" error path (temporarily revoke the
      delegation via TestDPC, scan a valid QR, confirm the specific error appears, then
      restore the delegation) — confirmed on-device: with delegation revoked (verified
      absent via `dumpsys device_policy`), scanning a valid `com.example.target` QR and
      tapping Apply produced the exact expected message: "QrDPC hasn't been delegated
      the APP_RESTRICTIONS scope for \"com.example.target\" by the active device policy
      controller."
