# MG4 Install

Native Android 9+ app for the MG4 head unit. Charcoal and mint styling follows the local MG4Zorst reference. The responsive grid shows every repository from https://github.com/stars/hojnikb/lists/mg4-apps with its description and install action.

## Use

Install `dist/MG4-Install-1.2.0-car.apk` once on a compatible MG4 head unit. Open MG4 Install, tap Install on a card, choose the correct APK variant, and confirm Download & install. Subsequent app installs are handed straight to Android's PackageInstaller; there is no Downloads-folder step. Use while parked.

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

## Version 1.2.0: ordering and firmware

Default Recommended order combines editorial general-purpose usefulness (15–35 points), logarithmic GitHub APK asset download counts (up to 30 points), and stable APK release age (+20 within 90 days, +10 within 180 days, -15 beyond a year, -30 beyond two years). No stable APK release date receives -10. Compatibility sections take precedence in every sorting mode: author-confirmed SWI165, unverified, explicitly unsupported, then reference projects. Usefulness is a transparent editorial heuristic, not a quality or compatibility certification. Most downloaded and Recently updated are selectable alternatives.

Counts are fetched from GitHub releases (up to the first 100 releases including prereleases), APK assets only. A + means the total may be incomplete. These are download events, not unique users or installs; repository-file downloads are not counted. Metadata is cached for 12 hours, fetched separately from the install worker, and retained on errors. Missing data is shown as unavailable, never fabricated as zero. Rate limits can prevent fresh statistics.

Auto-detection reads ro.build.mt2712.version, falling back to ro.build.version.incremental / Build.VERSION.INCREMENTAL, following MG4Control v2.6.5. Strict SWI/DBI prefixes recognize 68, 69, 131, 132, 133, 165 and 173. Unknown values disable installation until manually selected. The raw build string is shown in the firmware dialog. Manual overrides persist and are visibly labeled; select Auto-detect again to clear one. Detection performs no vehicle writes. See MG4Control-LICENSE.MIT.

Compatibility comes from the curated README and release-note review in COMPATIBILITY.md and compatibility-review.json (27 projects, reviewed 16 November 2026). Names and descriptions are never compatibility evidence. Confirmation is restricted to reviewed release tags and GitHub asset IDs; new tags and replacement assets remain unverified. Latest-tag metadata moves newer unreviewed releases out of the confirmed section when refreshed; cached project cards show historical evidence, while APK selection always checks the actual release and asset ID. Explicitly unsupported reviewed APKs are blocked in both UI and installer service. Unverified APKs require an explicit author-instructions acknowledgement. Other firmware families remain unverified because this review covers SWI165. Tap a compatibility label for limitations and source links. This is author-documented support, not on-car validation.

Run ./test.ps1 for parser and ranking/firmware regression tests. Use ./setup-public-test-keys.ps1 to obtain public AOSP signing test keys if needed.

## Version 1.2.0 validation

Parser tests and 21 firmware/evidence/ranking checks pass. Both car and emulator APKs pass apksigner verification. Emulator smoke checks covered the live 27-project feed, four confirmed apps above the unverified section, MG4Dash source-linked evidence with beta limitations, live v1.7.3 asset identification and the disabled install action before acknowledgement. Actual SWI165 vehicle installation remains untested. Reviewed compatibility is author documentation, not a guarantee of all features on every revision.
