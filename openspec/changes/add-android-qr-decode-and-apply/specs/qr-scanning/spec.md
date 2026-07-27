## ADDED Requirements

### Requirement: Camera-based QR capture
The app SHALL show a live camera preview and continuously analyze frames for QR codes
using ML Kit Barcode Scanning restricted to the QR code format, once camera permission
is granted.

#### Scenario: Preview shown after permission granted
- **WHEN** camera permission is granted
- **THEN** a live camera preview is displayed and frame analysis begins

#### Scenario: Only QR codes are recognized
- **WHEN** the camera frames contain a non-QR barcode (e.g. a UPC/EAN barcode)
- **THEN** it is not reported as a detected code

### Requirement: Raw bytes decoded explicitly as UTF-8
On detecting a QR code, the app SHALL read its raw bytes and decode them as UTF-8
itself (falling back to the scanning library's own decoded string only if raw bytes are
unavailable), then strip a leading UTF-8 BOM if present, per `qr-payload-format`.

#### Scenario: BOM is stripped before further processing
- **WHEN** the decoded string begins with a UTF-8 BOM character
- **THEN** that leading character is removed before the payload is parsed as JSON

### Requirement: Scanning pauses after a detection
Once a QR code has been detected and handed off for decoding, frame analysis SHALL
pause until the user chooses to rescan, so the same code isn't repeatedly re-triggered
while its result is being shown.

#### Scenario: No re-trigger while confirmation or error is shown
- **WHEN** a QR code has just been detected and its result (confirmation screen or
  error) is being displayed
- **THEN** further camera frames are not analyzed until the user taps a rescan action

#### Scenario: Rescan resumes analysis
- **WHEN** the user taps a rescan action from the confirmation or error state
- **THEN** frame analysis resumes and a new QR code can be detected
