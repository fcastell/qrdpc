# qr-config-generator Specification

## Purpose
Defines the static web app under `web/` that builds a Managed Configuration
(App Restrictions) payload via a generic key/type/value form and renders it as a QR
code for QrDPC to scan — fully offline, client-side only.

## Requirements

### Requirement: Generic restrictions form
The web app SHALL provide a form to enter a target `packageName` and to add, edit, and
remove restriction rows, each with a key, a type selector (`bool`, `string`, `integer`,
`choice`, `multi-select`, `bundle`, `bundle_array`), and a value input appropriate to
that type, without requiring prior knowledge of the target app's restriction schema.

#### Scenario: Adding a restriction row
- **WHEN** the user clicks the add-restriction control
- **THEN** a new row appears with an empty key, a type selector defaulted to `string`,
  and a value input for that type

#### Scenario: Value input matches the selected type
- **WHEN** a row's type is set to `bool`
- **THEN** its value input is a checkbox/toggle rather than a free-text field

#### Scenario: Bundle/bundle_array use a JSON sub-editor
- **WHEN** a row's type is `bundle` or `bundle_array`
- **THEN** its value is entered via an embedded JSON textarea, validated as a JSON
  object (`bundle`) or a JSON array of objects (`bundle_array`)

### Requirement: QR code generation and export
The app SHALL render the current form state as a QR code encoding the payload defined
by the `qr-payload-format` capability, and SHALL offer downloading it as PNG or SVG and
copying the underlying JSON to the clipboard.

#### Scenario: QR reflects current form state
- **WHEN** the user changes any field in the form and triggers generation
- **THEN** the rendered QR code encodes the payload matching the form's current values

#### Scenario: Export options are available
- **WHEN** a QR code has been generated
- **THEN** the user can download it as a PNG, download it as an SVG, and copy the raw
  JSON payload to the clipboard

### Requirement: Oversized payload is blocked, not truncated
If the minified JSON payload exceeds the vendored QR library's maximum byte-mode
capacity at the app's chosen error-correction level, the app SHALL block QR generation
and display the payload's byte size to the user, rather than silently truncating or
splitting it.

#### Scenario: Payload too large for one QR code
- **WHEN** the minified payload exceeds the maximum encodable size
- **THEN** QR generation is blocked and an error showing the byte count is displayed

### Requirement: Fully offline, client-side-only operation
The app SHALL perform all payload construction and QR rendering in the browser, with no
network requests after the initial page load, so configuration data never leaves the
user's device.

#### Scenario: No network calls during use
- **WHEN** the page has finished loading and the user builds a config and generates a
  QR code
- **THEN** no network requests are made for any of that interaction

#### Scenario: QR library is vendored, not CDN-loaded
- **WHEN** the page loads
- **THEN** the QR-generation library is served from this repo's own static assets, not
  fetched from a third-party CDN at runtime
