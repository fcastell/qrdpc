## ADDED Requirements

### Requirement: Form state persisted across page loads
The app SHALL save the current form state (target `packageName` and every restriction
row's key, type, and value, regardless of whether it currently validates) to the
browser's `localStorage` whenever it changes, and SHALL restore that state
automatically when the page loads, so the user's last edited config is not lost on
reload. If no saved state exists, or it cannot be parsed, the form SHALL fall back to
the default single empty `string` row.

#### Scenario: Edits are saved automatically
- **WHEN** the user edits the package name or any restriction row's key, type, or value
- **THEN** the current form state is written to `localStorage` without requiring an
  explicit save action

#### Scenario: State is restored on reload
- **WHEN** the page is loaded and a previously saved form state exists in
  `localStorage`
- **THEN** the form is populated from that saved state (package name and every
  restriction row) instead of the default empty row

#### Scenario: Missing or corrupt saved state falls back to the default
- **WHEN** the page is loaded and there is no saved state, or the saved state cannot be
  parsed
- **THEN** the form shows its normal default (empty package name, one empty `string`
  row)
