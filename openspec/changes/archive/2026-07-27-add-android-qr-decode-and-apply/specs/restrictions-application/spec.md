## ADDED Requirements

### Requirement: Payload validation against qr-payload-format
A decoded QR payload SHALL be validated against the `qr-payload-format` contract before
being presented for confirmation: `schemaVersion` present and equal to a supported
value (`1`), `packageName` a non-empty string, `restrictions` an array whose entries
each have a non-empty `key`, a `type` in the documented set (`bool`, `string`,
`integer`, `choice`, `multi-select`, `bundle`, `bundle_array`), and a `value` whose JSON
shape matches that type. Any violation SHALL abort with a specific, human-readable error
identifying what was wrong, not a generic "invalid QR code" message.

#### Scenario: Malformed JSON is rejected
- **WHEN** the decoded string is not valid JSON
- **THEN** an error is shown stating the content could not be parsed, and no
  confirmation screen is shown

#### Scenario: Unsupported schema version is rejected
- **WHEN** `schemaVersion` is present but not a version this app supports
- **THEN** an error names the unsupported version, and no confirmation screen is shown

#### Scenario: Type/value mismatch is rejected
- **WHEN** a restriction's `type` is `integer` and its `value` is not a JSON number
- **THEN** an error identifies the offending key, and no confirmation screen is shown

### Requirement: Bundle construction from a valid payload
A validated payload's `restrictions` SHALL be converted into an `android.os.Bundle`:
`bool`→boolean, `string`/`choice`→string, `integer`→int, `multi-select`→string array,
`bundle`→nested `Bundle` (values inside inferred from their native JSON type),
`bundle_array`→array of nested `Bundle`s (same inference applied to each element's
fields).

#### Scenario: Multi-select becomes a string array
- **WHEN** a restriction has `"type": "multi-select"` and `"value": ["a", "b"]`
- **THEN** the resulting Bundle entry for that key is a `String[]` containing `"a"`,
  `"b"`

#### Scenario: Nested bundle values are inferred by native JSON type
- **WHEN** a `bundle`-type restriction's value is `{"timeout": 30, "debug": false}`
- **THEN** the nested Bundle has an int entry `timeout=30` and a boolean entry
  `debug=false`, without those nested entries needing their own explicit `type` tag

### Requirement: Explicit confirmation before applying
The app SHALL show a confirmation screen listing the target package name and every
restriction about to be applied before calling into `DevicePolicyManager`, and SHALL
NOT apply anything without the user tapping an explicit Apply action.

#### Scenario: Confirmation shows real content
- **WHEN** a payload has been validated and converted to a Bundle
- **THEN** the confirmation screen displays the target `packageName` and each
  restriction's key, type, and value

#### Scenario: Apply requires explicit confirmation
- **WHEN** a QR code has been successfully scanned and validated
- **THEN** `DevicePolicyManager.setApplicationRestrictions` is not called until the user
  taps the Apply action on the confirmation screen

### Requirement: Delegated application with distinct error states
On Apply, the app SHALL first verify the target package is installed, then call
`DevicePolicyManager.setApplicationRestrictions(null, packageName, bundle)` (the
delegated-app call form), and SHALL distinguish "target package not installed" from
"delegation not granted" (`SecurityException`) from any other failure, each with its
own actionable message and a way to rescan.

#### Scenario: Target package not installed
- **WHEN** the user taps Apply and the target `packageName` is not installed on the
  device
- **THEN** an error states the target app isn't installed, and
  `setApplicationRestrictions` is never called

#### Scenario: An installed target package is correctly recognized regardless of prior knowledge
- **WHEN** the target `packageName` is installed on the device, whether or not it is a
  package this app has any prior relationship with
- **THEN** the installed-check recognizes it as installed (package-visibility
  restrictions on `PackageManager` queries do not cause a false "not installed" result)

#### Scenario: Delegation not granted
- **WHEN** the user taps Apply, the target package is installed, and
  `setApplicationRestrictions` throws `SecurityException`
- **THEN** an error states this app hasn't been delegated the `APP_RESTRICTIONS` scope
  for that package by the active device policy controller

#### Scenario: Successful application
- **WHEN** the user taps Apply, the target package is installed, and this app holds the
  `APP_RESTRICTIONS` delegation for it
- **THEN** `setApplicationRestrictions` is called with the converted Bundle, and success
  is shown to the user
