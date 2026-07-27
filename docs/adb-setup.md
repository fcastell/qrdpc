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

With winget:

```powershell
winget install --id Google.PlatformTools
```

Or with Chocolatey:

```powershell
choco install adb
```

Either installer adds `adb` to your `PATH` automatically. Otherwise, follow the manual
install steps below.

## Manual install

### macOS / Linux

Download the platform-tools zip for your OS from
[developer.android.com/tools/releases/platform-tools](https://developer.android.com/tools/releases/platform-tools),
extract it anywhere, and add that folder to your `PATH` (e.g. append `export
PATH="$PATH:/path/to/platform-tools"` to your shell profile).

### Windows

1. Download the Windows platform-tools zip from
   [developer.android.com/tools/releases/platform-tools](https://developer.android.com/tools/releases/platform-tools).
2. Extract it to a permanent location, e.g. `C:\platform-tools`.
3. Add that folder to your `PATH`:
   - Press **Win**, search for "Edit the system environment variables", open it.
   - Click **Environment Variables…**.
   - Under **User variables** (or **System variables** to apply for all users),
     select **Path** → **Edit…** → **New**, and add `C:\platform-tools`.
   - Click **OK** on every dialog to save.
4. Open a **new** PowerShell or Command Prompt window (the `PATH` change doesn't apply
   to already-open terminals) and verify with `adb version`.

Alternatively, from PowerShell, for the current user only:

```powershell
[Environment]::SetEnvironmentVariable("Path", $env:Path + ";C:\platform-tools", "User")
```

(also requires opening a new terminal window to take effect).

## Verify the install

```bash
adb version
```

## Connect a device

The device itself needs Developer options and USB debugging enabled first — see
[docs/enable-developer-mode.md](enable-developer-mode.md). Once that's done, connect
it via USB and confirm it's visible:

```bash
adb devices
```

The device should be listed with status `device` (not `unauthorized` — if it shows
`unauthorized`, check the device screen for the debugging prompt).

See [docs/installation.md](installation.md) for using `adb` to install QrDPC and set
up TestDPC.
