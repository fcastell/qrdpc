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

Early scaffold. The camera permission flow is wired up; QR scanning (CameraX + ML Kit
Barcode Scanning) and the actual mechanism for applying the decoded restrictions bundle
(most likely via the `DELEGATION_APP_RESTRICTIONS` scope delegated by the active DPC) are
not implemented yet.

## Requirements

- Android 8.0 (API 26) or later.
- The active Device Policy Controller must delegate the `DELEGATION_APP_RESTRICTIONS` scope
  to this app's package (`io.github.fcastell.qrdpc`) for the target app you want to
  configure — QrDPC does not act as its own DPC.

## Building

```bash
./gradlew :app:installDebug
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).
