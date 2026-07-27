---
name: verify-on-device
description: Installs and visually verifies QrDPC on a real Android device via adb (build, cold start, screenshots, navigation). Use after a UI implementation to confirm rendering/navigation before considering a task done.
metadata:
  author: fabien-castell
  version: "1.0"
---

QrDPC has no fixed target hardware — any real, connected, debug-enabled Android device is
acceptable (no emulator as the sole reference). This skill verifies a UI implementation on
a real device before considering it done.

## 1. Prerequisites

- `adb` must be on `PATH`.
- A device connected over USB, in debug mode: `adb devices -l` must list at least one
  authorized device. If nothing shows up, stop and ask the user to connect/unlock a
  device rather than falling back to an emulator.

## 2. Build + install

```bash
./gradlew :app:installDebug
```

No need to uninstall first unless the signature changed (keep it simple, `installDebug`
reinstalls on top).

## 3. Cold start

```bash
adb shell am force-stop io.github.fcastell.qrdpc
adb shell am start -n io.github.fcastell.qrdpc/.MainActivity
```

Capture one or two screenshots right after `am start` (brief splash/first-frame state) and
one after ~1s (settled screen):

```bash
adb exec-out screencap -p > <scratchpad>/explicit_name.png
```

Always use an explicit filename in the session's scratchpad directory (not a generic name
reused across captures), then read the file with the Read tool to inspect it visually —
never consider a UI verification done without actually having looked at the image.

## 4. Navigation / interaction

- Tap: `adb shell input tap <x> <y>`
- Swipe (scroll, opening the app drawer): `adb shell input swipe <x1> <y1> <x2> <y2>`
- Back / home: `adb shell input keyevent KEYCODE_BACK` /
  `adb shell input keyevent KEYCODE_HOME`
- After each significant interaction (navigating to a new screen, going back, etc.), take
  another screenshot to confirm the result rather than assuming the action worked.

## 5. Comparison

- If the implementation follows a mockup, compare the resulting screenshot to it visually
  — not just "the app renders", but actual fidelity (colors, text, layout).
- For anything involving the camera permission flow or QR scanning UI, verify both the
  granted and not-yet-granted states render as expected.

## Quick reference

- Package: `io.github.fcastell.qrdpc` / main activity: `.MainActivity`.
- Don't conclude a UI task is done based solely on a passing `assemble`/`test` — this
  skill is the real visual verification step that goes with it.
