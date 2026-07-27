## Context

`.restriction-row-main` (key, type, remove button) is a CSS grid whose children stretch
to fill their column by default. The value control below it, `.restriction-row-value`,
is a plain block `<div>` with the raw `<input>`/`<textarea>` inserted directly by
`renderValueControl()` in `app.js` — with no `width: 100%` rule, a text input's
intrinsic browser-default width (historically ~20 characters) applies instead, which is
visibly narrower than the row above it (reported by the user with a screenshot).

Separately, the tool currently has zero persistence — every reload starts from one
blank `string` row and an empty package name, by original design (`add-qr-config-generator`
explicitly called this out as a non-goal: "no persistence... stateless between page
loads"). The user now wants the opposite: the last edited config restored automatically.

## Goals / Non-Goals

**Goals:**
- Value fields visually match the width of the key/type row above them.
- The form's current state (package name + every restriction row, whatever their
  current — possibly incomplete/invalid — content) survives a page reload via
  `localStorage`, with no explicit "save" action required.

**Non-Goals:**
- No multi-slot storage (e.g. separate saved configs per package name, a history list,
  named presets) — a single "last state" slot, matching "the last config" as asked.
- No migration/versioning scheme for the stored shape — if it ever fails to parse back
  (e.g. changed shape in a future version), fall back to the normal empty-form default
  silently rather than erroring.

## Decisions

- **Fix via a general `width: 100%` rule** on the existing
  `input[type="text"], input[type="number"], select, textarea` selector in
  `style.css`, rather than a narrow selector scoped to `.restriction-row-value` — this
  is simpler, and every one of this tool's text-like inputs is already meant to fill its
  container (`box-sizing: border-box` is already global, so no overflow risk from
  padding).
- **Autosave on every input/change event**, not only on "Generate" — captures
  in-progress edits (e.g. a half-typed key, or a row whose JSON textarea doesn't parse
  yet), since the point is resuming exactly where the user left off, not just recalling
  the last successfully-generated payload. Implemented via one delegated listener on the
  restrictions list container plus one on the package name field, rather than per-field
  listeners re-wired on every row add/remove.
- **Storage shape is the raw form model, not the validated `qr-payload-format`
  payload**: `{ packageName: string, rows: [{ key, type, value }] }`, where `value` is
  whatever the raw control held (string for text/textarea controls, boolean for the
  bool checkbox) — deliberately not run through the generator's own JSON-payload
  validation before saving, so an incomplete/invalid draft still round-trips.
- **Single `localStorage` key** (`qrdpc-generator-last-config`), overwritten on every
  save — no per-package-name keying. "Restore my last config" is the whole ask; keeping
  it to one slot avoids needing any UI for managing multiple saved states.
- **Restore replaces the default single empty row**: on page load, if a saved state
  exists and parses, the form is built from it (0..N rows, package name filled in)
  instead of the usual one-empty-`string`-row default. If nothing is saved, or the saved
  JSON is malformed, fall back to today's default silently.

## Risks / Trade-offs

- [`localStorage` is per-browser-profile, not synced across devices] → Acceptable;
  matches the "your data never leaves your device" property the tool already commits to
  — syncing would require a server, explicitly out of scope for this tool.
- [A stored draft with invalid JSON in a bundle/bundle_array textarea silently
  round-trips as invalid] → Intentional (see Decisions) — the existing per-field
  validation error UI already handles showing that as invalid once the user hits
  Generate; nothing new needed here.

## Migration Plan

1. Add the CSS width fix.
2. Add save-on-change and restore-on-load logic to `app.js`.
3. Manually verify in a browser: edit a config, reload, confirm it's restored;
   clear `localStorage` (or use a private window), reload, confirm the normal empty
   default appears.

## Open Questions

None outstanding — scope confirmed directly by the user's request.
