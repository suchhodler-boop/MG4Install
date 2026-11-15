# MG4 Install

Native Android 9+ app for the MG4 head unit. Charcoal and mint styling follows the local MG4Zorst reference. The responsive grid shows every repository from https://github.com/stars/hojnikb/lists/mg4-apps with its description and install action.

## Use

Install `dist/MG4-Install-1.0.0-car.apk` once on a compatible MG4 head unit. Open MG4 Install, tap Install on a card, choose the correct APK variant, and confirm Download & install. Subsequent app installs are handed straight to Android's PackageInstaller; there is no Downloads-folder step. Use while parked.

The car build uses `android.uid.system` and the AOSP platform test certificate, as MG4Dash does. This only works on firmware that accepts that certificate. It does not bypass Android signature, downgrade, ABI, or SDK checks. The emulator build omits the system UID and uses a separate package ID; ordinary Android may request unknown-source permission and installation confirmation.

## Feed and downloads

- Fetches the public GitHub star-list HTML on launch and Refresh, follows pagination, and saves the last successful feed for offline browsing. GitHub does not expose public star-list membership through its REST API. Markup changes are reported without deleting the saved feed.
- Searches titles, developer names, and descriptions locally.
- Fetches eligible releases on demand, choosing the newest release containing APK assets (up to 90 releases). Prereleases are opt-in in About. Multiple APK variants are shown for explicit selection.
- When no release APK exists, scans the default-branch tree at a pinned commit for APK files. Projects without APKs retain their source link. Git LFS pointer files fail APK validation.
- Uses HTTPS, bounded network timeouts, a 1 GB APK cap, APK validation, and SHA-256 verification whenever GitHub provides an asset digest. Android performs certificate and compatibility verification.
- Foreground service keeps a download running while the UI is backgrounded. Cancel download interrupts at the next network read (up to the 30-second read timeout). Install commits cannot be cancelled from this button.
- Unauthenticated GitHub API limits apply. Rate-limit and network failures appear in the footer. Downloads are not automatically retried or silently installed.

## Build

Run `./build.ps1` in PowerShell, or `./build.ps1 -Emulator`. Supply `-Sdk` and `-Jdk` when paths differ. Requires Android SDK platform 34, build-tools 36.0.0, and JDK 17+. The build uses javac, D8, aapt2 and apksigner directly; no Gradle cache or external library download is needed. All build data stays in this project on E:.

Place the public AOSP Android 9 platform test key (`platform.pk8` and `platform.x509.pem`) in `keys/`. The key is intentionally excluded from Git. MG4Dash documents its source at https://github.com/Skittle6938/MG4Dash#building . Never substitute a private production key in source control.

## Attribution and license

GPL-3.0; see LICENSE. PackageInstaller internal-storage/session flow in InstallService.java is adapted from Skittle6938/MG4Dash `app/src/main/java/app/mg4dash/system/RadioDashUpdater.kt`. Upstream source and commit are recorded in THIRD_PARTY.md. The remaining app is newly written. MG4 Install is an independent community project, not an official MG or GitHub app.

## Validation limitations

The emulator can verify rendering, networking and the ordinary Android installation path. Actual system-UID installation must be verified on the user's compatible MG4 head unit. No vehicle settings or installed car applications are changed during development.
