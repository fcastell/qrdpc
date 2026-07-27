## 1. GitHub Pages setup

- [x] 1.1 Enable GitHub Pages on `fcastell/qrdpc` with `build_type: "workflow"` via
      `gh api`
- [x] 1.2 Verify Pages is enabled (`gh api repos/fcastell/qrdpc/pages`) before relying on
      it in the deploy workflow

## 2. Web app scaffold

- [x] 2.1 Create `web/` with `index.html`, a stylesheet, and an entry JS file (plain
      HTML/CSS/vanilla JS, no build step)
- [x] 2.2 Vendor a small, dependency-free, MIT-licensed QR code generator (e.g.
      `kazuhikoarase/qrcode-generator`) into `web/vendor/`, keeping its license file
      alongside it

## 3. Restrictions form

- [x] 3.1 Build the `packageName` input and the add/remove restriction row controls
- [x] 3.2 Implement per-row type selector (`bool`, `string`, `integer`, `choice`,
      `multi-select`, `bundle`, `bundle_array`) with a value input matching each type
      (checkbox for `bool`, number input for `integer`, comma/tag input for
      `multi-select`, plain text for `string`/`choice`)
- [x] 3.3 Implement the embedded JSON textarea sub-editor for `bundle`
      (object) and `bundle_array` (array of objects), with inline validation errors

## 4. Payload building and validation

- [x] 4.1 Implement building the `{schemaVersion, packageName, restrictions}` JSON
      payload from the current form state, matching the `qr-payload-format` spec
- [x] 4.2 Validate each restriction's value against its declared type before allowing QR
      generation (reject type/value mismatches with an inline error)
- [x] 4.3 Compute the minified payload's byte size and block generation with a clear
      byte-count error when it exceeds the vendored library's max byte-mode capacity at
      error-correction level M (implemented via try/catch around the library's own
      overflow detection rather than a hardcoded threshold — see design.md note)

## 5. QR generation and export

- [x] 5.1 Render the payload as a QR code (ECC level M, auto-sized version) on demand
- [x] 5.2 Add PNG download, SVG download, and "copy JSON to clipboard" actions for the
      current QR/payload
- [x] 5.3 Confirm no network requests occur after initial page load (check via browser
      devtools network tab while building a config and generating a QR code) — verified
      via `claude-in-chrome`: only 4 same-origin requests on load (index.html, style.css,
      app.js, vendored qrcode.js), zero additional requests while filling all 7
      restriction types and generating a QR code; payload JSON matched the
      `qr-payload-format` spec exactly

## 6. Deployment pipeline

- [x] 6.1 Add `.github/workflows/pages.yml` (build + deploy jobs using
      `actions/configure-pages`, `actions/upload-pages-artifact`,
      `actions/deploy-pages`), triggered on push to `main` touching `web/**` plus
      `workflow_dispatch`
- [x] 6.2 Add `web/**` to `.github/workflows/ci.yml`'s `paths-ignore`

## 7. End-to-end verification

- [x] 7.1 Open a PR with all of the above, confirm the Android CI job is skipped
      (web-only change) and merge once ready — this PR itself also modifies
      `.github/workflows/ci.yml` (not covered by `paths-ignore`), so the Android job
      correctly ran rather than skipped here; it passed (spotlessCheck, detekt, test,
      lint, assemble all green). Future web-only PRs that don't touch `ci.yml` will skip
      it, per the `ci-pipeline` delta spec's scenario
- [ ] 7.2 After merge, confirm `pages.yml` runs successfully and the site is live at
      `https://fcastell.github.io/qrdpc/`
- [x] 7.3 Build a sample restrictions config covering every type (including nested
      `bundle`/`bundle_array`), generate its QR code, scan/decode it with any QR reader,
      and confirm the decoded JSON matches the `qr-payload-format` spec and the form's
      input exactly — done with `zbarimg` (real independent decoder, not just re-running
      our own JS). First pass surfaced two real bugs with non-ASCII values (accented
      characters came back corrupted): (1) the library's `stringToBytesFuncs['default']`
      reassignment trick didn't work — fixed by reassigning `qrcode.stringToBytes`
      directly; (2) even correct UTF-8 byte-mode data gets misread by at least one
      real-world scanner without a charset hint — fixed by prefixing the payload with a
      UTF-8 BOM before encoding, now documented as part of the `qr-payload-format`
      contract. Re-verified after both fixes: all 7 types (including accented text in
      `string`, `choice`, `multi-select`, and nested `bundle` values) decoded back
      byte-for-byte correct
