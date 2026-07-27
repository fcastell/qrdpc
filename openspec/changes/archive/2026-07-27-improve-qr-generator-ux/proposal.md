## Why

Two small usability gaps in `qr-config-generator`, reported directly by the user while
using the tool: the restriction row's value field renders narrower than the key/type
fields above it (it isn't wrapped in the same flex layout that stretches inputs to full
width), and the form has no memory — every page load starts from a single blank
restriction row, even if the user is iterating on the same config across multiple
visits.

## What Changes

- Fix the value input/textarea width: restriction rows' value field SHALL span the same
  width as the key/type row above it (currently narrower, since it isn't in the same
  stretching flex layout).
- Persist the current form state (package name + all restriction rows, as entered — not
  just successfully-generated configs) to the browser's `localStorage` on every change,
  and restore it automatically on page load, so returning to the tool picks up where the
  user left off. This reverses the "no persistence" non-goal from the original
  `add-qr-config-generator` design, per explicit user request.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `qr-config-generator`: adds a requirement for persisting and restoring form state via
  `localStorage`.

## Impact

- Affected paths: `web/style.css` (value field width), `web/app.js` (save/restore
  logic).
- No new dependencies, no server/network involvement — `localStorage` is local to the
  browser, consistent with the tool's fully-offline, client-side-only operation
  (`qr-config-generator`'s existing requirement).
