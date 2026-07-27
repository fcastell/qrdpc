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
