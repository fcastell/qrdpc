## Why

Testing the latest deploy of the `web/` QR config generator, the user got old cached
`app.js`/`style.css` from the browser's HTTP cache and had to force a hard reload to
see the new version. GitHub Pages serves these files with short but non-zero
`Cache-Control` freshness (and browsers commonly hold onto them longer via heuristic
caching), so a redeploy doesn't reliably reach users without a manual cache-bust.

## What Changes

- The Pages deploy workflow (`.github/workflows/pages.yml`) SHALL append a
  commit-SHA-based cache-busting query string (e.g. `?v=<short-sha>`) to the
  `style.css`, `vendor/qrcode-generator/qrcode.js`, and `app.js` references in
  `web/index.html` at build time, before uploading the Pages artifact — so every deploy
  serves a unique URL for its assets and browsers fetch fresh copies without requiring
  a manual hard reload.
- This is a build-time-only transformation on the artifact uploaded to Pages; the
  `web/index.html` file committed to the repo is untouched (no query strings checked
  in), keeping the source clean and framework-free.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `web-tool-hosting`: adds a requirement that deployed asset references are
  cache-busted per deploy so redeploys are immediately visible without manual cache
  clearing.

## Impact

- Affected paths: `.github/workflows/pages.yml` (new build step).
- No changes to `web/` source files themselves, no new dependencies, no server
  involvement — the cache-busting is a plain-text substitution done by the CI runner
  before the static artifact is uploaded.
