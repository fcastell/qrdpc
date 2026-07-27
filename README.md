# QrDPC

A small Android app that reads a QR code encoding Android Managed Configuration
(App Restrictions) values and pushes them to a target app — no more clicking through a
DPC's UI by hand.

## Motivation

Testing an app that reads its configuration from Android Managed Configuration (Intune App
Restrictions, or the equivalent `RestrictionsManager` values pushed by any EMM/DPC) usually
means opening a DPC app such as [TestDPC](https://github.com/googlesamples/android-testdpc),
navigating to its "Managed configurations" screen, and manually re-entering values every
time they need to change. There's no simple `adb` command for this, and the TestDPC UI is
easy to get wrong (unsaved edits are lost if you navigate away before hitting the outer save
button).

QrDPC removes that friction: encode the restrictions bundle you want as a QR code, scan it
with this app, and it applies the values directly — repeatable, shareable, and scriptable
(any QR code generator can produce the input).

## Status

QR scanning (CameraX + ML Kit Barcode Scanning) and applying the decoded restrictions
bundle via the delegated `DevicePolicyManager.setApplicationRestrictions` call are
implemented. A companion [web-based QR config generator](https://fcastell.github.io/qrdpc/)
builds the QR codes QrDPC scans — fully offline, no server involved.

## Requirements

- Android 8.0 (API 26) or later.
- The active Device Policy Controller must delegate the `APP_RESTRICTIONS` scope to
  this app's package (`io.github.fcastell.qrdpc`) for the target app you want to
  configure — QrDPC does not act as its own DPC. See
  [docs/installation.md](docs/installation.md) for a full setup guide, including
  installing TestDPC and granting this delegation.

## Building

```bash
./gradlew :app:installDebug
```

See [docs/](docs/README.md) for installing on a device and setting up a DPC to
delegate to QrDPC.

## Development setup

This repo uses [lefthook](https://github.com/evilmartians/lefthook) to auto-format staged
Kotlin files before each commit. After cloning, install it once (e.g. `brew install
lefthook` on macOS) and wire up the git hooks:

```bash
lefthook install
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
