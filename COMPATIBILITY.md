# SWI165 compatibility review

Reviewed 16 November 2026. All 27 listed project READMEs and all available published release-note histories were checked. Confirmation means explicit author documentation, not testing on this user’s car. Firmware revision, hardware, vendor packages and individual features still matter. Missing evidence is unverified, not incompatible.

The live project feed and release downloads remain live. This evidence snapshot is curated; new tags or replaced assets require fresh review and never inherit release confirmation. Confirmed releases are identified by tag and GitHub asset ID. All sorting modes keep compatibility groups first, then sort within each group.

## Author-confirmed SWI165

### SliDeeN/MG4Control

Tagged README explicitly lists SWI165 as compatible; v2.5.0 introduced SWI165 support. Available features depend on vehicle equipment and vendor services.

Reviewed stable APK release: v2.6.10. Published release histories checked: 30. [README](https://github.com/SliDeeN/MG4Control/blob/v2.6.10/README.md) · [Release notes](https://github.com/SliDeeN/MG4Control/releases/tag/v2.6.10).

### malys/EVTasker

Tagged README explicitly supports SWI165 climate, charging, radio/calls and equipped heating features. Runtime diagnostics determine actual available bindings; not every action is supported.

Reviewed stable APK release: v2.18.4. Published release histories checked: 35. [README](https://github.com/malys/EVTasker/blob/v2.18.4/README.md) · [Release notes](https://github.com/malys/EVTasker/releases/tag/v2.18.4).

### Skittle6938/MG4Dash

SWI165 support is beta and user-tested: climate, charging/limit, cable lock and Keep climate on are confirmed in the tagged README. v1.7.3 includes older-firmware climate fixes; this does not confirm every feature or the separate radio add-on.

Reviewed stable APK release: v1.7.3. Published release histories checked: 15. [README](https://github.com/Skittle6938/MG4Dash/blob/v1.7.3/README.md) · [Release notes](https://github.com/Skittle6938/MG4Dash/releases/tag/v1.7.3).

### ernestomicco2026-cell/HVAC-Clima-MG4

Tagged README includes SWI165 in the SWI68 climate family; release history records recognition of SWI165. OEM HVAC versions .41131/.47141 are documented; verify other OEM revisions and installed equipment.

Reviewed stable APK release: 1.4.2. Published release histories checked: 9. [README](https://github.com/ernestomicco2026-cell/HVAC-Clima-MG4/blob/1.4.2/README.md) · [Release notes](https://github.com/ernestomicco2026-cell/HVAC-Clima-MG4/releases/tag/1.4.2).

## Unverified for SWI165

### Skittle6938/winclose

Documented tests cover SWI69 and several other revisions, but not SWI165. Window APIs vary by firmware revision.

Reviewed stable APK release: v1.2.0. Published release histories checked: 2. [README](https://github.com/Skittle6938/winclose/blob/HEAD/README.md) · [Release notes](https://github.com/Skittle6938/winclose/releases/tag/v1.2.0).

### Skittle6938/MG4-Radio-DABplus-Fix

Legacy radio fix; successor is MG4Radio. Requires a compatible stock Radio_eh32_ll package; no SWI165 confirmation.

Reviewed stable APK release: none. Published release histories checked: 0. [README](https://github.com/Skittle6938/MG4-Radio-DABplus-Fix/blob/HEAD/README.md) · [Release notes](https://github.com/Skittle6938/MG4-Radio-DABplus-Fix/releases).

### merthankaraman/DriveHub_Dort

README and release notes do not confirm SWI165. Vehicle integrations and platform signing require verification.

Reviewed stable APK release: v0.23. Published release histories checked: 4. [README](https://github.com/merthankaraman/DriveHub_Dort/blob/HEAD/README.md) · [Release notes](https://github.com/merthankaraman/DriveHub_Dort/releases/tag/v0.23).

### jamakr4/MG4-HVAC-Overlay-Launcher

Requires the stock SystemUI IPageService climate interface. No SWI165 confirmation.

Reviewed stable APK release: v1.0. Published release histories checked: 1. [README](https://github.com/jamakr4/MG4-HVAC-Overlay-Launcher/blob/HEAD/README.md) · [Release notes](https://github.com/jamakr4/MG4-HVAC-Overlay-Launcher/releases/tag/v1.0).

### lkernan/ABRP_Uploader

MG4 EH32 integration is described, but no SWI165 confirmation or published releases.

Reviewed stable APK release: none. Published release histories checked: 0. [README](https://github.com/lkernan/ABRP_Uploader/blob/HEAD/README.md) · [Release notes](https://github.com/lkernan/ABRP_Uploader/releases).

### lkernan/ADBUtil

Documented for SE/Excite 2022–2025 with USB OTG. No SWI165 confirmation.

Reviewed stable APK release: none. Published release histories checked: 0. [README](https://github.com/lkernan/ADBUtil/blob/HEAD/README.md) · [Release notes](https://github.com/lkernan/ADBUtil/releases).

### lkernan/CarMediaPlayer

Author describes an Excite 51 media-player replacement. No SWI165 confirmation or published releases.

Reviewed stable APK release: none. Published release histories checked: 0. [README](https://github.com/lkernan/CarMediaPlayer/blob/HEAD/README.md) · [Release notes](https://github.com/lkernan/CarMediaPlayer/releases).

### adammcdonagh/MG4-Custom-Launcher

README reports testing on Trophy SWI68 R67 only. This is not evidence of SWI165 support.

Reviewed stable APK release: v0.1. Published release histories checked: 1. [README](https://github.com/adammcdonagh/MG4-Custom-Launcher/blob/HEAD/README.md) · [Release notes](https://github.com/adammcdonagh/MG4-Custom-Launcher/releases/tag/v0.1).

### paswotz/mg4-headunit-ui-switch

README restricts use to MG4 SE / EU Standard models. No SWI165 confirmation; check trim as well as firmware.

Reviewed stable APK release: v105. Published release histories checked: 5. [README](https://github.com/paswotz/mg4-headunit-ui-switch/blob/HEAD/README.md) · [Release notes](https://github.com/paswotz/mg4-headunit-ui-switch/releases/tag/v105).

### jamakr4/MG4-360-Camera-App

Requires compatible factory 360-camera hardware. Firmware upgrade instructions are not SWI165 support evidence.

Reviewed stable APK release: v0.8.14. Published release histories checked: 13. [README](https://github.com/jamakr4/MG4-360-Camera-App/blob/HEAD/README.md) · [Release notes](https://github.com/jamakr4/MG4-360-Camera-App/releases/tag/v0.8.14).

### Tommasov/MG4_Swipe

README and all release notes contain no explicit SWI165 confirmation. Check signing and accessibility requirements.

Reviewed stable APK release: 1.5. Published release histories checked: 5. [README](https://github.com/Tommasov/MG4_Swipe/blob/HEAD/README.md) · [Release notes](https://github.com/Tommasov/MG4_Swipe/releases/tag/1.5).

### frasertag/MG4-HVACFloat

README and release notes describe HVAC features without confirming SWI165.

Reviewed stable APK release: v0.5.4. Published release histories checked: 8. [README](https://github.com/frasertag/MG4-HVACFloat/blob/HEAD/README.md) · [Release notes](https://github.com/frasertag/MG4-HVACFloat/releases/tag/v0.5.4).

### jamakr4/MG4-Dashcam-Trigger

Companion to a compatible 360-camera app; camera-app compatibility must be established first. No SWI165 confirmation.

Reviewed stable APK release: v1.0. Published release histories checked: 1. [README](https://github.com/jamakr4/MG4-Dashcam-Trigger/blob/HEAD/README.md) · [Release notes](https://github.com/jamakr4/MG4-Dashcam-Trigger/releases/tag/v1.0).

### X-Ryl669/MG4-Custom-Launcher

README reports testing on Trophy SWI68 R67 only. No SWI165 confirmation.

Reviewed stable APK release: v1.0.0. Published release histories checked: 1. [README](https://github.com/X-Ryl669/MG4-Custom-Launcher/blob/HEAD/README.md) · [Release notes](https://github.com/X-Ryl669/MG4-Custom-Launcher/releases/tag/v1.0.0).

### fatihdonmezdev/DiPlay-MG4

Tested target is SWI69; other firmware is untested. SWI165 telemetry fixes do not confirm complete CarPlay/Android Auto compatibility.

Reviewed stable APK release: v0.2.9-mg4.62. Published release histories checked: 11. [README](https://github.com/fatihdonmezdev/DiPlay-MG4/blob/HEAD/README.md) · [Release notes](https://github.com/fatihdonmezdev/DiPlay-MG4/releases/tag/v0.2.9-mg4.62).

### Skittle6938/MG4-AndroidAuto-BootFix

Author compatibility table explicitly marks SWI165 unknown because of its different software base.

Reviewed stable APK release: v1.3.0. Published release histories checked: 4. [README](https://github.com/Skittle6938/MG4-AndroidAuto-BootFix/blob/HEAD/README.md) · [Release notes](https://github.com/Skittle6938/MG4-AndroidAuto-BootFix/releases/tag/v1.3.0).

### ernestomicco2026-cell/mg4-taskmanager

SWI68-specific accessibility instructions are documented, but no SWI165 confirmation.

Reviewed stable APK release: v1.3. Published release histories checked: 5. [README](https://github.com/ernestomicco2026-cell/mg4-taskmanager/blob/HEAD/README.md) · [Release notes](https://github.com/ernestomicco2026-cell/mg4-taskmanager/releases/tag/v1.3).

### Tommasov/MG4_Simple_Launcher

README and release history do not explicitly confirm SWI165.

Reviewed stable APK release: 1.7. Published release histories checked: 6. [README](https://github.com/Tommasov/MG4_Simple_Launcher/blob/HEAD/README.md) · [Release notes](https://github.com/Tommasov/MG4_Simple_Launcher/releases/tag/1.7).

### Skittle6938/MG4Radio

SWI165 fixes are mentioned, but documented on-car testing is SWI69. Requires a compatible stock radio package; older-firmware fixes are not full-app validation.

Reviewed stable APK release: v1.6.3. Published release histories checked: 19. [README](https://github.com/Skittle6938/MG4Radio/blob/HEAD/README.md) · [Release notes](https://github.com/Skittle6938/MG4Radio/releases/tag/v1.6.3).

## Explicitly unsupported on SWI165

### ernestomicco2026-cell/Mg4-Launcher-SWI68

README explicitly says this edition works ONLY with SWI68 SAIC 1.0+ and must not be installed on other firmware.

Reviewed stable APK release: v1.0.3. Published release histories checked: 28. [README](https://github.com/ernestomicco2026-cell/Mg4-Launcher-SWI68/blob/HEAD/README.md) · [Release notes](https://github.com/ernestomicco2026-cell/Mg4-Launcher-SWI68/releases/tag/v1.0.3).

### ernestomicco2026-cell/Mg4-Launcher-SWI69

README explicitly limits this edition to SWI69 Color Radio; firmware-specific editions are not interchangeable.

Reviewed stable APK release: v0.4.3. Published release histories checked: 26. [README](https://github.com/ernestomicco2026-cell/Mg4-Launcher-SWI69/blob/HEAD/README.md) · [Release notes](https://github.com/ernestomicco2026-cell/Mg4-Launcher-SWI69/releases/tag/v0.4.3).

## Reference projects

### jamakr4/Diagnostic-for-MG4

Diagnostic trouble-code JSON/reference project, not a head-unit APK.

Reviewed stable APK release: none. Published release histories checked: 1. [README](https://github.com/jamakr4/Diagnostic-for-MG4/blob/HEAD/README.md) · [Release notes](https://github.com/jamakr4/Diagnostic-for-MG4/releases).

### MG-EV-Hackers/Open-Vehicle-Monitoring-System-3-MGEV

OVMS firmware/hardware project, not a head-unit APK. Linked Android clients are separate projects.

Reviewed stable APK release: none. Published release histories checked: 0. [README](https://github.com/MG-EV-Hackers/Open-Vehicle-Monitoring-System-3-MGEV/blob/HEAD/README.md) · [Release notes](https://github.com/MG-EV-Hackers/Open-Vehicle-Monitoring-System-3-MGEV/releases).
