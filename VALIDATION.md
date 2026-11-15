# Tests performed

- Feed parser: unrelated links excluded; descriptions decoded; absent descriptions handled; pagination decoded and constrained to the specified list; changed markup rejected.
- Live list fixture: 27 repositories parsed. Emulator live HTTPS refresh also displayed all 27.
- Emulator at 1920 x 768: charcoal/mint grid, descriptions and install controls visually inspected.
- APK discovery: winclose v1.2.0, winclose-signed.apk, 11.8 MB offered in picker; explicit confirmation displayed.
- No-APK path: ADBUtil displayed source-link fallback.
- Both car and emulator APKs compiled and passed apksigner verification.
- Ordinary-device install permission killed the test process as expected when changed by appops. UI copy now explains selecting the APK again after an Android restart.
- The shared emulator exited during the subsequent install test. End-to-end download/installation and final prerelease UI rendering were therefore NOT verified. No car installation was attempted. Test devices are not connected at completion.

## Version 1.1.0 validation

- Parser suite and 16 firmware/ranking assertions pass: SWI165, case/whitespace, known DBI prefix, unknown/null builds, numeric-prefix collision, project/APK mismatches, explicit multi-target names, unverified labels, recency penalty, popularity weighting, mismatch-last recommendations, download/date order.
- Signed car and emulator APKs pass apksigner verification.
- Isolated API 30 emulator loads 27 live projects and live GitHub APK counters (MG4Control 5,123, MG4Dash 1,014 at test time). Most downloaded sort verified in UI.
- Non-car build 7193139 correctly detected as unknown; manual SWI165 selection visibly labeled. SWI69 launcher install blocked with firmware-mismatch dialog.
- All emulator disk data and logs are under project .scratch on E:. Actual vehicle property read and car installation still require verification on an MG4.
- Compatibility confirmation verified: Download & install is disabled before the acknowledgement checkbox is selected. Picker separator encoding corrected after visual inspection.
