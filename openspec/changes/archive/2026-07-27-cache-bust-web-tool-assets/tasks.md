## 1. Add cache-busting step to the Pages deploy workflow

- [x] 1.1 In `.github/workflows/pages.yml`'s `build` job, add a step right after
      `actions/checkout` and before `actions/configure-pages` that appends
      `?v=${GITHUB_SHA::7}` to the `style.css`, `vendor/qrcode-generator/qrcode.js`,
      and `app.js` references in the runner's checked-out `web/index.html`
- [x] 1.2 Verify locally: copy `web/index.html`, run the same substitution logic
      against the copy, and confirm the three asset references gain the `?v=` suffix
      while the rest of the file (including the repo's actual `web/index.html`) is
      unchanged

## 2. Verify the live deploy

- [x] 2.1 After merge, once `pages.yml` has run for the merge commit, view the live
      page's HTML source (or `curl`) and confirm `style.css`, `qrcode.js`, and `app.js`
      are referenced with the expected `?v=` suffix. Confirmed via
      `curl https://fcastell.github.io/qrdpc/`: all three carry `?v=e3dde5f`, matching
      the merge commit.
