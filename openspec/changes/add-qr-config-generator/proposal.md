## Why

QrDPC can scan a QR code, but nothing today produces one in the format the app expects.
Without a companion generator, using QrDPC means hand-crafting a QR code payload with a
generic QR tool and no guarantee it matches the schema QrDPC will decode. A small,
static, serverless web tool — hosted for free on GitHub Pages, modeled on tools like
https://mattintech.github.io/tools/android-qr/index.html (works fully offline after
first load, configuration data never leaves the device) — closes that loop: build a
restrictions bundle in a form, get a QR code, scan it with QrDPC. It also gives the
project something to point non-technical testers at without asking them to install
anything.

## What Changes

- Define the QR code payload format QrDPC (Android side) will decode and the generator
  will produce: a JSON object with a target `packageName` and a list of typed
  restrictions (`bool`, `string`, `integer`, `choice`, `multi-select`, `bundle`,
  `bundle_array`), mirroring Android's `RESTRICTION_TYPE_*` values used by
  `RestrictionsManager`/App Restrictions schemas. This is a contract shared by the future
  Android-side decoder (not implemented yet) and the new web generator.
- Add a static web app under `web/`: a generic key/type/value form to build the
  restrictions list (add/remove rows, per-row type selector, nested JSON editor for the
  `bundle`/`bundle_array` types), a package name field, and a QR code preview with
  PNG/SVG download and "copy JSON" options.
- The web app runs entirely client-side: no backend, no network calls after the page
  loads (QR generation library vendored into the repo, not loaded from a CDN at
  runtime), so configuration data — including anything sensitive — never leaves the
  browser.
- Warn in the UI (and block generation) when the encoded payload exceeds a safe QR code
  capacity, since large restrictions bundles may not fit in a single scannable code.
- Host the tool on GitHub Pages via a GitHub Actions deploy workflow
  (`actions/upload-pages-artifact` + `actions/deploy-pages`), published at
  `https://fcastell.github.io/qrdpc/`, triggered on pushes to `main` that touch `web/**`.
- Extend the existing Android CI workflow's `paths-ignore` to also skip `web/**` (the web
  tool has its own deploy workflow and doesn't need the Android build/lint/test job).

## Capabilities

### New Capabilities
- `qr-payload-format`: the JSON schema/contract for what a QrDPC QR code encodes
  (target package name + typed restrictions list), independent of which side
  (generator or scanner) implements it.
- `qr-config-generator`: the static web app itself — the restrictions form, QR code
  rendering/export, and its fully offline, client-side-only operation.
- `web-tool-hosting`: the GitHub Pages deployment pipeline for the web app.

### Modified Capabilities
- `ci-pipeline`: add `web/**` to the Android CI workflow's `paths-ignore`, since web-tool
  changes are covered by their own deploy workflow, not the Android build.

## Impact

- Affected paths: new `web/` directory (HTML/CSS/JS + vendored QR library), new
  `.github/workflows/pages.yml`, `.github/workflows/ci.yml` (`paths-ignore` update).
- Affected systems: GitHub Pages (new site for this repo), GitHub Actions (new workflow).
- No impact on the Android app's existing code — the Android-side QR *decoding* logic
  that consumes this payload format is a separate, not-yet-implemented piece of work;
  this change only defines the contract and builds the generator side of it.
