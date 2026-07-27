# Installing adb (Android Platform Tools)

`adb` ships as part of Google's Android Platform Tools package. This is a one-time
setup on your development machine, independent of any specific Android device.

## macOS

```bash
brew install --cask android-platform-tools
```

## Linux

Debian/Ubuntu:

```bash
sudo apt install android-tools-adb
```

Fedora:

```bash
sudo dnf install android-tools
```

If your distro's package is outdated, download the platform-tools zip directly (see
below).

## Windows

```powershell
choco install adb
```

Or download the platform-tools zip directly (see below) and add its folder to your
`PATH`.

## Manual install (any OS)

Download the platform-tools zip for your OS from
[developer.android.com/tools/releases/platform-tools](https://developer.android.com/tools/releases/platform-tools),
extract it anywhere, and add that folder to your `PATH`.

## Verify the install

```bash
adb version
```

## Connect a device

1. On the Android device, enable Developer Options (Settings → About phone → tap
   "Build number" 7 times).
2. In Developer Options, enable **USB debugging**.
3. Connect the device via USB and accept the "Allow USB debugging?" prompt that
   appears on the device.
4. Confirm it's visible:

   ```bash
   adb devices
   ```

   The device should be listed with status `device` (not `unauthorized` — if it shows
   `unauthorized`, check the device screen for the debugging prompt).

See [docs/installation.md](installation.md) for using `adb` to install QrDPC and set
up TestDPC.
