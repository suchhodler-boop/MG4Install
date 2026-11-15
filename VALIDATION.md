# Tests performed

- Feed parser: unrelated links excluded; descriptions decoded; absent descriptions handled; pagination decoded and constrained to the specified list; changed markup rejected.
- Live list fixture: 27 repositories parsed. Emulator live HTTPS refresh also displayed all 27.
- Emulator at 1920 x 768: charcoal/mint grid, descriptions and install controls visually inspected.
- APK discovery: winclose v1.2.0, winclose-signed.apk, 11.8 MB offered in picker; explicit confirmation displayed.
- No-APK path: ADBUtil displayed source-link fallback.
- Both car and emulator APKs compiled and passed apksigner verification.
- Ordinary-device install permission killed the test process as expected when changed by appops. UI copy now explains selecting the APK again after an Android restart.
- The shared emulator exited during the subsequent install test. End-to-end download/installation and final prerelease UI rendering were therefore NOT verified. No car installation was attempted. Test devices are not connected at completion.
