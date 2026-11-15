# SYSTIVRA screen-by-screen implementation and 1:1 QA matrix

**Product rule:** only Splash precedes Dashboard. All visible UI is English. No login, registration, pricing, onboarding or fake health/performance values.

References exported: **37**. Each PNG is a **concept rendering**, not a verified app screenshot.

| # | Screen reference | Android route / functionality | Pixel-perfect verified |
|---|---|---|---|
| 01 | [Splash](../brending/screens/01-splash.svg) | Routable; partial functionality | **No** |
| 02 | [System Health](../brending/screens/02-system-health.svg) | Routable; partial functionality | **No** |
| 03 | [Device Detection](../brending/screens/03-device-detection.svg) | Routable; partial functionality | **No** |
| 04 | [Privacy Center](../brending/screens/04-privacy-center.svg) | Routable; partial functionality | **No** |
| 05 | [Dashboard](../brending/screens/05-dashboard.svg) | Routable; partial functionality | **No** |
| 06 | [Hardware Explorer](../brending/screens/06-hardware-explorer.svg) | Routable; partial functionality | **No** |
| 07 | [Cpu Details](../brending/screens/07-cpu-details.svg) | Routable; partial functionality | **No** |
| 08 | [Cpu Cores](../brending/screens/08-cpu-cores.svg) | Routable; partial functionality | **No** |
| 09 | [Gpu Details](../brending/screens/09-gpu-details.svg) | Routable; partial functionality | **No** |
| 10 | [Memory Details](../brending/screens/10-memory-details.svg) | Routable; partial functionality | **No** |
| 11 | [Storage Details](../brending/screens/11-storage-details.svg) | Routable; partial functionality | **No** |
| 12 | [Battery Details](../brending/screens/12-battery-details.svg) | Routable; partial functionality | **No** |
| 13 | [Charging Analysis](../brending/screens/13-charging-analysis.svg) | Routable; partial functionality | **No** |
| 14 | [Thermal Details](../brending/screens/14-thermal-details.svg) | Routable; partial functionality | **No** |
| 15 | [Display Details](../brending/screens/15-display-details.svg) | Routable; partial functionality | **No** |
| 16 | [Device And Android](../brending/screens/16-device-and-android.svg) | Routable; partial functionality | **No** |
| 17 | [Network Details](../brending/screens/17-network-details.svg) | Routable; feature scope varies | **No** |
| 18 | [Sensor Explorer](../brending/screens/18-sensor-explorer.svg) | Routable; partial functionality | **No** |
| 19 | [Sensor Live](../brending/screens/19-sensor-live.svg) | Routable; feature scope varies | **No** |
| 20 | [App Usage](../brending/screens/20-app-usage.svg) | Routable; feature scope varies | **No** |
| 21 | [Live Monitor](../brending/screens/21-live-monitor.svg) | Routable; partial functionality | **No** |
| 22 | [Metric Fullscreen](../brending/screens/22-metric-fullscreen.svg) | Routable; partial functionality | **No** |
| 23 | [Recording Session](../brending/screens/23-recording-session.svg) | Routable; feature scope varies | **No** |
| 24 | [Session Analysis](../brending/screens/24-session-analysis.svg) | Routable; feature scope varies | **No** |
| 25 | [Alerts](../brending/screens/25-alerts.svg) | Routable; feature scope varies | **No** |
| 26 | [Diagnostics Center](../brending/screens/26-diagnostics-center.svg) | Routable; partial functionality | **No** |
| 27 | [Component Test](../brending/screens/27-component-test.svg) | Routable; partial functionality | **No** |
| 28 | [Diagnostic Results](../brending/screens/28-diagnostic-results.svg) | Routable; feature scope varies | **No** |
| 29 | [Benchmark Center](../brending/screens/29-benchmark-center.svg) | Routable; feature scope varies | **No** |
| 30 | [Benchmark Running](../brending/screens/30-benchmark-running.svg) | Routable; feature scope varies | **No** |
| 31 | [Benchmark Results](../brending/screens/31-benchmark-results.svg) | Routable; feature scope varies | **No** |
| 32 | [Overlay Configuration](../brending/screens/32-overlay-configuration.svg) | Routable; feature scope varies | **No** |
| 33 | [History](../brending/screens/33-history.svg) | Routable; partial functionality | **No** |
| 34 | [Reports Export](../brending/screens/34-reports-export.svg) | Routable; partial functionality | **No** |
| 35 | [Permission Center](../brending/screens/35-permission-center.svg) | Routable; feature scope varies | **No** |
| 36 | [Settings](../brending/screens/36-settings.svg) | Routable; partial functionality | **No** |
| 37 | [About](../brending/screens/37-about.svg) | Routable; partial functionality | **No** |


## Validation requirements

1. On Android devices capture screenshots for each implemented route at 360×800, 390×844, 412×915, 600×960 and 800×1280, with controlled densities, font scales, OS version and insets.
2. Compare against approved reference renders with the [pixel difference script](../tools/visual-diff.py); archive the reference, actual screenshot, visual diff, mean error and changed-pixel percentage.
3. Retain a route-to-reference mapping and verify functionality and accessibility independently of pixel similarity.
4. Resolve card dimensions, elevation, typography, icon shapes, safe area, status/navigation bars, themes, landscape and large screens.
5. **Never claim a 1:1 match** without automated comparison plus manual on-device review.

**Current result:** 0/37 screens certified pixel-perfect. A real screenshot evidence workflow was added; no 1:1 claim until emulator and physical-device comparisons pass. Screens partially implemented do not equal finished functional modules.

## What remains outside the 37 routable destinations

- Cross-application overlay / foreground-service permissions are **not implemented**. The HUD page is an interactive in-app preview only.
- Recorded history exists in private SQLite. Additional long-term analytics, restricted GPU/CPU temperatures and broader benchmarks remain future work.
- Test results for microphone, speaker, camera and all hardware sensors are not yet implemented.
- Do not treat nonfunctional concepts as production-complete merely because their routes exist.

## v0.4.0 data retention clarification

Foreground-only user recordings are now stored in an app-private SQLite database, capped at 900 samples. Recording stops when the app is backgrounded. The user can clear saved samples or export them to CSV through a system file picker. This does not establish visual 1:1 certification.

## v0.5 session-management addition

The Recording Library is an additional real in-app screen beyond the original 37 design references. It inherits the shared Compose design system but has **no approved image baseline yet**; do not count its implementation as visual 1:1 compliance. The existing 37 design references and new Recording Library both require real screenshot comparison and device tests. The display/touch and vibration diagnostics remain manually user-confirmed.

## v0.6 analytics and visual scope

**Session Insights** is an additional screen beyond the 37 reference concepts. The recording library and Session Insights have no approved pixel-perfect baseline and must not be called 1:1. Automated emulator capture now targets 38 app screens (36 original in-app references plus these two extra screens). The Android system Splash must be captured and reviewed independently. Actual image diffs, touch interactions, font scaling and multiple device densities remain mandatory acceptance gates.

## Reference alignment — initial Android implementation pass

The first code changes in the `feat/systivra-v100-reference-parity-play-previews` development branch match the **reference layout structure** for Dashboard and Hardware Explorer: headings, SYSTIVRA ring card, four primary metric cards, palette, border radius and text-only navigation bar. Additional functionality remains available below the fold. This is a first implementation pass, **not an achieved pixel-perfect certification**; exact font metrics, inset alignment and all other routes still require automated screenshot comparisons and device testing.

The original 37 references are preserved under `brending/screens/*.svg` and available as 1080 × 1920 previews (side margins only; no stretching or cropping) in `brending/google-play/reference-previews-NOT-PLAY-SCREENSHOTS/`. They must not be submitted as real Play screenshots.

An invalid Android build workflow configuration was identified (artifact-upload `with:` attached directly to a `run` step). That syntax has been fixed and CI re-enabled for this branch. Screenshot QA remains a separate acceptance gate.
