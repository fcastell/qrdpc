## ADDED Requirements

### Requirement: JSON payload envelope
A QrDPC QR code SHALL encode a JSON object with a `schemaVersion` integer, a
`packageName` string (the target app's Android package name), and a `restrictions`
array, so that any QrDPC-compatible generator or scanner can produce or consume a
config without ambiguity.

#### Scenario: Valid payload structure
- **WHEN** a payload is `{"schemaVersion": 1, "packageName": "com.example.target",
  "restrictions": [...]}`
- **THEN** it is a well-formed QrDPC payload

#### Scenario: Missing required field is invalid
- **WHEN** a payload is missing `schemaVersion`, `packageName`, or `restrictions`
- **THEN** it is not a valid QrDPC payload

### Requirement: Typed restriction entries
Each entry in `restrictions` SHALL be an object with a `key` string, a `type` string
one of `bool`, `string`, `integer`, `choice`, `multi-select`, `bundle`, `bundle_array`,
and a `value` whose JSON shape matches that type: boolean for `bool`; string for
`string` and `choice`; integer number for `integer`; array of strings for
`multi-select`; JSON object for `bundle`; array of JSON objects for `bundle_array`.

#### Scenario: multi-select value is a string array
- **WHEN** a restriction has `"type": "multi-select"`
- **THEN** its `value` is a JSON array of strings

#### Scenario: bundle value is a JSON object
- **WHEN** a restriction has `"type": "bundle"`
- **THEN** its `value` is a JSON object, whose own nested value types are inferred from
  their native JSON type rather than further tagged

#### Scenario: Type mismatch is invalid
- **WHEN** a restriction has `"type": "integer"` and a `value` that is not a JSON number
- **THEN** the entry is not a valid restriction

### Requirement: Restriction key order is preserved
`restrictions` SHALL be a JSON array (not a map), so that the order in which
restrictions were authored is preserved end to end.

#### Scenario: Order round-trips
- **WHEN** a payload's `restrictions` array lists keys in a given order
- **THEN** consumers reading the array observe that same order

### Requirement: UTF-8 BOM prefix for QR byte-mode transport
The QR code's byte-mode data SHALL be the UTF-8 encoding of the JSON payload prefixed
with a UTF-8 byte order mark (U+FEFF, encoded as `EF BB BF`). QR byte-mode segments
carry no charset marker of their own, and real-world scanners have been observed
(verified with `zbar`) to fall back to a non-UTF-8 default charset for non-ASCII content
without this hint, corrupting accented/non-Latin characters on decode. A scanner
SHALL strip the leading BOM before parsing the remaining bytes as JSON.

#### Scenario: Non-ASCII restriction values decode correctly
- **WHEN** a restriction's `string`/`choice`/`multi-select`/`bundle` value contains
  non-ASCII characters (e.g. accented Latin text) and the QR is decoded by a
  BOM-aware, UTF-8-capable scanner
- **THEN** the decoded JSON matches the original payload exactly, with no character
  corruption

#### Scenario: Scanner strips the BOM before parsing
- **WHEN** the raw decoded QR bytes begin with `EF BB BF`
- **THEN** a scanner removes those three bytes before running its JSON parser, since a
  leading BOM character is not valid at the start of a JSON document
