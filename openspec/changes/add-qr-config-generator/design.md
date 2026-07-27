## Context

QrDPC's Android side can (eventually) scan a QR code and apply the decoded values as an
Android Managed Configuration (App Restrictions) bundle to a target app, but nothing in
this repo defines what that QR code actually encodes, and there's no tool to produce one.
The reference tool the user pointed at
(https://mattintech.github.io/tools/android-qr/index.html) is actually a different thing
— an Android Enterprise *device provisioning* QR generator (enrollment credentials for
factory-reset setup) — but it demonstrates the pattern QrDPC wants to copy: a small,
static, GitHub-Pages-hosted page that works fully offline after first load and never
sends the (potentially sensitive) configuration data anywhere.

This change defines the QR payload format and builds the generator side of that loop. The
Android-side decoder that consumes this format is separate, not-yet-implemented work
(tracked by the app's existing "not implemented yet" status in `README.md`); this design
only needs to produce a format that decoder can later target.

## Goals / Non-Goals

**Goals:**
- Define a JSON payload format for "target package + typed restrictions list" that
  mirrors Android's `RESTRICTION_TYPE_*` values (`bool`, `string`, `integer`, `choice`,
  `multi-select`, `bundle`, `bundle_array`), so it maps cleanly onto
  `RestrictionsManager`/`Bundle` on the Android side later.
- Ship a static, dependency-free (at runtime) web page that builds that JSON via a
  generic key/type/value form and renders it as a scannable QR code.
- Host it on GitHub Pages, deployed via GitHub Actions, at
  `https://fcastell.github.io/qrdpc/`.

**Non-Goals:**
- No Android-side QR decoding implementation — that's separate future work.
- No JS framework or bundler for v1 — plain HTML/CSS/vanilla JS, even though deployment
  goes through GitHub Actions (chosen for its own reasons — see Decisions). A bundler can
  be introduced later without changing the deploy mechanism.
- No multi-QR pagination for oversized payloads — if a payload is too large for one QR
  code, the UI blocks generation with an error rather than splitting across codes.
- No persistence (no local storage of previously built configs) — the tool is stateless
  between page loads.

## Decisions

- **Payload envelope**:
  ```json
  {
    "schemaVersion": 1,
    "packageName": "com.example.target",
    "restrictions": [
      { "key": "isEnabled", "type": "bool", "value": true },
      { "key": "serverUrl", "type": "string", "value": "https://example.com" },
      { "key": "maxRetries", "type": "integer", "value": 3 },
      { "key": "mode", "type": "choice", "value": "production" },
      { "key": "allowedDomains", "type": "multi-select", "value": ["a.com", "b.com"] },
      { "key": "advanced", "type": "bundle", "value": { "timeout": 30, "debug": false } },
      { "key": "servers", "type": "bundle_array", "value": [{ "host": "a" }] }
    ]
  }
  ```
  `schemaVersion` is included from the start for forward compatibility, even though only
  `1` exists today. `restrictions` is a list (not a map) so key order is preserved and
  matches the order restrictions were added in the form.
- **Type system mirrors Android's `RESTRICTION_TYPE_*`** (`bool`, `string`, `integer`,
  `choice`, `multi-select`, `bundle`, `bundle_array`) at the top level of each
  restriction, since that's what a real `app_restrictions.xml` on a target app declares.
  `choice` is encoded identically to `string` (a single string value) — the tool doesn't
  need to know the target app's allowed choice list, only the chosen value.
  `multi-select` is a JSON array of strings.
- **Nested `bundle`/`bundle_array` values are raw JSON, not further typed**: editing a
  `bundle` or `bundle_array` restriction in the form drops into a small embedded JSON
  textarea (object for `bundle`, array of objects for `bundle_array`) rather than a
  recursive key/type/value form. A future Android-side decoder infers nested value types
  from JSON's own types (object → nested `Bundle`, boolean → `putBoolean`, string →
  `putString`, number → `putInt`). This keeps the form UI simple for the common flat
  cases while still supporting nested structures when a target app needs them.
- **QR generation library is vendored, not CDN-loaded**: a small, dependency-free,
  MIT-licensed JS QR encoder (`kazuhikoarase/qrcode-generator`) is copied into
  `web/vendor/` with its license kept alongside it, so the page has zero runtime network
  requests after the initial load — matching the "configuration data never leaves your
  device" property of the reference tool.
- **QR byte-mode data is prefixed with a UTF-8 BOM (`EF BB BF`)** — discovered during
  implementation testing (real decode with `zbar`, not just re-parsing our own JS
  output): the vendored library emits raw byte-mode data with no ECI/charset marker, and
  at least one real-world scanner falls back to a non-UTF-8 default charset for
  non-ASCII bytes without a hint, corrupting accented characters on decode (verified:
  `héllo wörld` came back as mangled CJK/katakana-range characters without the BOM,
  decoded perfectly with it). The vendored library's own `stringToBytesFuncs['UTF-8']`
  helper is also not wired up correctly by simply reassigning the `'default'` entry in
  its map — `qr8BitByte` reads the already-bound `qrcode.stringToBytes` reference
  captured at library load time, so `qrcode.stringToBytes` itself must be reassigned.
  Both fixes together were verified with a real independent decode (not just re-running
  our own encoder logic) across all 7 restriction types including nested
  `bundle`/`bundle_array` values with non-ASCII content. This is now part of the
  `qr-payload-format` contract, not just an implementation detail, since a future
  Android-side decoder must also strip the BOM before parsing.
- **Error correction level M, auto-sized QR version**: generate at ECC level M (~15%
  recovery — a reasonable balance of scan reliability vs. capacity for a phone camera)
  and let the library pick the smallest QR version (1–40) that fits the payload. If the
  minified JSON payload exceeds that library's max byte-mode capacity at level M (~2331
  bytes), block generation and show the byte count so the user can trim restrictions.
- **GitHub Pages via Actions, not "deploy from branch"**: even though the site is static
  files with no build step in v1, hosting is provisioned as `build_type: "workflow"` on
  the GitHub Pages API, deployed via `actions/configure-pages` +
  `actions/upload-pages-artifact` + `actions/deploy-pages` in a dedicated
  `.github/workflows/pages.yml`. This keeps the door open for a future build step
  (bundler, minification, a small framework) without having to migrate the hosting
  mechanism later.
- **`web/` as a top-level directory**, sibling to `app/` and `openspec/`, kept entirely
  separate from the Gradle project (no shared tooling, no shared CI job). The Android
  `ci.yml` workflow's `paths-ignore` is extended to include `web/**` so web-only changes
  don't trigger an unrelated Android build.
- **`pages.yml` triggers only on push to `main` touching `web/**`** (plus
  `workflow_dispatch` for manual redeploys), consistent with `git-pr-workflow`: changes
  land on `main` via PR, and only then does the live site update.

## Risks / Trade-offs

- [A target app's actual `app_restrictions.xml` uses a restriction type this format
  doesn't cleanly express] → Not expected given the type list mirrors Android's own
  `RESTRICTION_TYPE_*` enum; if a gap is found later, `schemaVersion` allows a
  non-breaking extension.
- [Vendored QR library goes stale / has an unpatched bug] → It's a small, self-contained
  encoder (no dependencies of its own) with low churn; acceptable for a client-side,
  non-security-critical rendering task. Mitigation: periodic manual check, same posture
  as the pinned GitHub Actions SHAs in `ci-pipeline`.
- [Large restrictions bundles don't fit in one QR code] → Explicitly a non-goal to solve
  via pagination; the UI blocking with a clear byte-count error is the accepted trade-off
  for v1.
- [GitHub Pages custom build_type requires one-time API/repo-settings setup] → Must be
  enabled once via `gh api` (or the repo settings UI) before the first `pages.yml` run
  succeeds; documented as an explicit task, not assumed to happen automatically.
- [Not every QR scanner respects a leading UTF-8 BOM in byte-mode data] → The BOM
  convention is widely supported (it's the standard workaround for this exact QR
  ecosystem ambiguity) and was verified against a real independent decoder, but it isn't
  part of the formal QR/ISO spec. Restriction values are realistically ASCII-heavy
  (package names, URLs, flags, numbers) so most payloads are unaffected either way,
  since ASCII bytes are identical with or without this convention. Mitigation if a
  future reader mishandles it: this is now a documented, versionable part of the
  `qr-payload-format` contract (`schemaVersion`) rather than a silent implementation
  detail.

## Migration Plan

1. Enable GitHub Pages on the repo with `build_type: "workflow"` (one-time, via `gh api`
   or repo settings).
2. Add `web/` with the form UI, vendored QR library, and payload-building logic.
3. Add `.github/workflows/pages.yml` (build + deploy jobs) and extend `ci.yml`'s
   `paths-ignore` with `web/**`.
4. Open a PR, confirm the Pages deploy job runs and publishes successfully, verify the
   live URL renders and a generated QR code decodes back to the expected JSON (using any
   QR reader) before merging.

No rollback beyond removing `web/` and `pages.yml` and disabling Pages if something is
fundamentally wrong — nothing else depends on this yet.

## Open Questions

None outstanding — payload format, editor style, target-package handling, and hosting
mechanism were confirmed by the user before this design was written.
