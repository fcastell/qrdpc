## Context

`web/index.html` references its assets by plain relative path
(`href="style.css"`, `src="vendor/qrcode-generator/qrcode.js"`, `src="app.js"`), with
no version or hash in the URL. GitHub Pages applies its own (short but non-zero)
`Cache-Control` freshness window, and browsers layer heuristic caching on top of that,
so a redeploy of `main` doesn't reliably invalidate what a returning visitor's browser
already has — confirmed directly: the user had to force a hard reload to see the
`improve-qr-generator-ux` changes after they were live.

The tool is deliberately server-free and framework-free (`web-tool-hosting`,
`qr-config-generator`'s "fully offline, client-side-only" requirement) — no bundler, no
Node build step exists today. `pages.yml` currently just uploads `web/` as-is.

## Goals / Non-Goals

**Goals:**
- Every deploy serves its JS/CSS assets at a URL unique to that deploy, so a returning
  browser fetches the new version instead of reusing a stale cached one — no manual
  hard-reload required.
- No new source-level build tooling (bundler, hashing pipeline, package.json) — keep
  `web/` itself framework-free; the transformation lives entirely in the existing
  GitHub Actions workflow.

**Non-Goals:**
- No content-hash-based filenames (e.g. `app.abcd1234.js`) — a commit-SHA query string
  is simpler to generate with a one-line `sed` step and achieves the same invalidation
  effect for this tool's scale.
- No changes to `Cache-Control` response headers — GitHub Pages doesn't expose control
  over these, and cache-busting via the URL itself sidesteps needing to.

## Decisions

- **Cache-bust via a build-time `sed` substitution in `pages.yml`**, appending
  `?v=<short-sha>` (`${GITHUB_SHA::7}`) to the `style.css`, `qrcode.js`, and `app.js`
  references in the copy of `web/index.html` that gets uploaded as the Pages artifact.
  Runs as a step between `actions/checkout` and `actions/configure-pages`, before
  `actions/upload-pages-artifact`.
  - Alternative considered: content-hashed filenames via a small hashing script —
    rejected as more moving parts for the same practical outcome at this project's
    scale (three static assets, no code-splitting).
  - Alternative considered: a `<meta http-equiv="Cache-Control">` tag or
    `_headers`/`netlify.toml`-style config — GitHub Pages doesn't honor per-file custom
    response headers, so this wouldn't work on this host.
- **Mutate only the checked-out copy in the CI runner, not the repository**: the
  substitution happens after `actions/checkout` in the `build` job's own workspace, and
  only that modified copy is uploaded via `upload-pages-artifact`. `web/index.html` in
  the repo stays free of query strings — no extra commit or bot-authored diff needed
  per deploy.
- **`index.html` itself is not cache-busted**: it's the entry point users navigate to
  directly by URL, so instead it should be served with a short/no-cache freshness
  window (which GitHub Pages already applies to HTML) — only the assets it references
  need busting, since those are what silently goes stale in the background.

## Risks / Trade-offs

- [`sed` pattern drifts from `index.html` if the markup changes shape] → Low risk given
  the file's small size and stability; if it ever silently no-ops, the symptom (stale
  cache again) matches today's status quo, not a regression — not silently harmful.
- [Short SHA collisions] → Not a practical concern at this repo's history length, and
  even a collision would just mean two deploys share a cache generation, not incorrect
  content.

## Migration Plan

1. Add the cache-busting step to `.github/workflows/pages.yml`.
2. Verify locally: run the same `sed` substitutions against a copy of `web/index.html`
   and confirm the three asset references gain the `?v=` suffix while the rest of the
   file is untouched.
3. Merge and let the next `web/**` push (or a manual `workflow_dispatch`) deploy;
   spot-check the live page's HTML source for the versioned asset URLs.
