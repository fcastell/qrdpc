## 1. Fix value field width

- [x] 1.1 Add `width: 100%` to the shared `input[type="text"], input[type="number"],
      select, textarea` rule in `web/style.css` so restriction value fields match the
      key/type row's width

## 2. Persist and restore form state

- [x] 2.1 Implement `saveState()`: read the current package name and every restriction
      row's key/type/value from the DOM, write as JSON to
      `localStorage["qrdpc-generator-last-config"]`
- [x] 2.2 Wire `saveState()` to fire on input/change events for the package name field
      and (via delegation) the restrictions list, so edits and add/remove-row actions
      are captured without per-row listener rewiring
- [x] 2.3 Implement `loadState()`: on page load, read and parse the saved state; if
      present and valid, rebuild the form from it (package name + each row, per-type
      value control); otherwise leave the default single empty row in place
- [x] 2.4 Verify manually in a browser: edit a config, reload, confirm it's restored;
      clear `localStorage` (or use a private window), reload, confirm the normal empty
      default appears
