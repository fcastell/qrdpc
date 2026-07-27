# Enabling Developer Mode and USB Debugging on Android

Developer options are hidden by default on Android. This is a one-time setup on the
device itself (separate from installing `adb` on your computer — see
[docs/adb-setup.md](adb-setup.md) for that).

## 1. Enable Developer options

1. Open **Settings** → **About phone** (some OEMs, e.g. Samsung, nest this under
   **About phone** → **Software information**).
2. Find **Build number**.
3. Tap **Build number** 7 times in a row. After a few taps you'll see a countdown
   toast ("You are now N steps away from being a developer."). You may be prompted for
   your device PIN/pattern/password to confirm.
4. **Developer options** now appears in **Settings** (usually under **System**, though
   its exact location varies by manufacturer and Android version).

## 2. Enable USB debugging

1. Open **Settings** → **Developer options**.
2. Enable **USB debugging**.
3. If present, also enable **Install via USB** — some OEMs require it for `adb
   install` to work.

## 3. Connect and authorize your computer

1. Connect the device to your computer via USB.
2. If prompted for a USB connection mode, choose **File Transfer (MTP)** — not
   required for `adb`, but avoids some OEMs limiting the connection under "Charging
   only".
3. A **"Allow USB debugging?"** dialog appears on the device, showing your computer's
   RSA key fingerprint. Accept it (optionally checking **Always allow from this
   computer** to skip this prompt on future connections).
4. From your computer, confirm the device is recognized:

   ```bash
   adb devices
   ```

   The device should be listed with status `device`. If it shows `unauthorized`,
   check the device screen for the debugging prompt (step 3) and accept it.

## Notes for managed/enterprise devices

On some rugged/enterprise devices (e.g. Zebra) or devices already enrolled under an
MDM/DPC, Developer options may be relocated, hidden, or have USB debugging disabled by
policy. If USB debugging can't be enabled after following the steps above, check with
whoever manages the device's enrollment policy.
