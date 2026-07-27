# ui-verification Specification

## Purpose
Defines the expectation that UI implementation work on QrDPC is confirmed visually on a
real Android device before being considered done, via the `verify-on-device` skill —
ported from `rms-mobile-app` and generalized since QrDPC has no fixed target hardware.

## Requirements

### Requirement: Real-device verification of UI changes
UI implementation work SHALL be verified on a real, connected Android device via `adb`
before being considered complete — never on the basis of a passing build/test run alone,
and never on an emulator as the sole verification.

#### Scenario: UI task is not marked done from build/test alone
- **WHEN** a UI-affecting change has only been confirmed via `./gradlew assemble` or a test
  run
- **THEN** the task is not yet considered complete

#### Scenario: Device is available before verification proceeds
- **WHEN** `adb devices -l` lists no connected, authorized device
- **THEN** verification stops and the user is asked to connect/unlock a device rather than
  falling back to an emulator

### Requirement: Screenshot-based inspection
Verification SHALL install the current build, cold-start the app, capture a screenshot
into the session scratchpad with an explicit filename, and the screenshot SHALL actually be
read and visually inspected — not assumed correct because the capture command succeeded.

#### Scenario: Screenshot is read after capture
- **WHEN** a screenshot is captured via `adb exec-out screencap`
- **THEN** the resulting file is read and visually inspected before the verification step
  is considered complete
