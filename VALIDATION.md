# Validation — version 1.4.0

- Feed-parser regression suite passes.
- 21 firmware, evidence and ranking checks pass.
- 20 updater/feed checks pass: supplemental sources offline, case-insensitive deduplication, upstream-description preservation, idempotent merge, newer-version comparison, stable HTTPS release URL restrictions, checksum format, matching signing identities, wrong package/signature/version rejection.
- Android instrumentation exercised actual signed APK metadata against the installed 1.3.0 emulator app: accepted the same-signed 1.4.0 upgrade; rejected same version, car package mismatch and expected-version mismatch.
- Both car and emulator builds pass apksigner verification.
- Emulator smoke checks at 1920 × 720: 29 catalog entries, larger fullscreen UI, Updates settings with automatic checks enabled and automatic installation disabled by default, retained preferences and package-replaced status.
- Previous 1.3.0 validation completed a real MG4-HVACFloat v0.5.4 download and Android installation. Compatibility warnings remain advisory.

## Limits

The Android test uses a separate emulator package and does not verify OEM system-UID installation, SWI detection, vehicle functionality or dock behavior on the actual car. Automatic checks run when the app opens, not as a continuous background daemon. A future car self-update still needs validation on compatible hardware. GitHub API limits and availability can affect live metadata.

All task emulator files and bounded logs are kept in the project scratch directory on E:. Test-owned emulators are shut down after validation.
