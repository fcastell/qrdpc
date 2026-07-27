# Installation and Test Setup

QrDPC doesn't act as its own Device Policy Controller (DPC) — it applies restrictions
through the `APP_RESTRICTIONS` delegation scope granted by whichever DPC is active on
the device. This guide covers a full test setup from a device with no DPC configured
yet: installing QrDPC, installing [TestDPC](https://github.com/googlesamples/android-testdpc)
as a sample DPC, creating a managed profile owned by it via `adb`, and delegating the
`APP_RESTRICTIONS` scope to QrDPC through TestDPC's UI.

If a DPC (TestDPC or a real EMM) is already active on your device and you only need to
grant the delegation, skip to [3. Delegate the APP_RESTRICTIONS scope to
QrDPC](#3-delegate-the-app_restrictions-scope-to-qrdpc).

## Prerequisites

- A device or emulator running Android 8.0 (API 26) or later.
- `adb` (Android Platform Tools) installed, with the device connected and USB
  debugging enabled (`adb devices` should list it) — see
  [docs/adb-setup.md](adb-setup.md) if you don't have it set up yet.
- The device has no existing accounts/profiles that would block a new managed
  profile from being created (a freshly set-up or factory-reset device is simplest).

## 1. Install QrDPC

From this repository:

```bash
./gradlew :app:installDebug
```

Or, from a release APK downloaded from the
[Releases page](https://github.com/fcastell/qrdpc/releases):

```bash
adb install qrdpc-release.apk
```

## 2. Install TestDPC and create a managed profile for it

TestDPC is Google's reference DPC app, useful for testing delegation without a real
EMM. If the device has Play Store access, install it from there:
[TestDPC on Google Play](https://play.google.com/store/apps/details?id=com.afwsamples.testdpc).

Otherwise (e.g. a rugged/enterprise device or emulator without Play Store), build it
from source and sideload it:

```bash
git clone https://github.com/googlesamples/android-testdpc.git
cd android-testdpc
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Create a managed (work) profile owned by TestDPC:

```bash
adb shell dpm create-managed-profile \
  --user 0 \
  --name "QrDPC test profile" \
  com.afwsamples.testdpc/.DeviceAdminReceiver
```

This prints something like `Success: created profile with user handle 10` — note that
user handle. Start the new profile if it isn't already running:

```bash
adb shell am start-user 10
```

On the device, complete TestDPC's on-screen provisioning prompts for the new profile
(accept the setup screens until they finish). The profile then shows up with a badged
"TestDPC" icon.

Both QrDPC and whichever app you intend to configure restrictions on need to exist
inside that profile. Install them there explicitly with the `--user` flag (using the
user handle from above):

```bash
adb install --user 10 app/build/outputs/apk/debug/app-debug.apk   # from this repo's build/, QrDPC
adb install --user 10 <path-to-target-app.apk>
```

## 3. Delegate the APP_RESTRICTIONS scope to QrDPC

Inside the managed profile, open the badged **TestDPC** app and grant QrDPC the
delegation scope it needs to apply restrictions on your behalf:

1. Open **TestDPC**.
2. Go to **App restrictions manager**.
3. Select **QrDPC** (`io.github.fcastell.qrdpc`) as the delegate app.
4. Tap **SET**.

QrDPC can now call `DevicePolicyManager.setApplicationRestrictions` on behalf of the
active DPC for apps in this profile. If this step is skipped or the delegation is
later revoked (e.g. after reinstalling QrDPC — delegation grants don't survive an
uninstall), QrDPC will show a "delegation not granted" error when you try to apply a
scanned configuration instead of failing silently.

## 4. Build a config and apply it

1. Open the [QR config generator](https://fcastell.github.io/qrdpc/), fill in the
   target app's package name and restrictions, and generate a QR code.
2. Open QrDPC on the device and scan it.
3. Review the confirmation screen and tap **Apply**.
