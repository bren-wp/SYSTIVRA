# Pixel-by-pixel verification protocol

**Current status: NOT VALIDATED**. Vector boards are design references; they are not assertions about actual screenshots.

1. Choose baseline devices (360×800, 390×844, 412×915, 600×960, 800×1280 physical px), density, font scale and insets.
2. Render each implemented Compose destination at each target. Capture **actual emulator/device screenshots**.
3. Compare with approved references after normalizing only documented system status/navigation-bar differences.
4. Calculate absolute pixel difference, perceptual image difference, layout alignment and text truncation.
5. Correct typography, glyph placement, icons, elevations, 3D/ring effects, card spacing and edge insets without altering functionality.
6. Record evidence per screen and keep failures visible in pull requests.

The 37 SVG templates are conceptual exports and do not substitute for reference screenshots or physical device testing.

## Automated emulator evidence

The [SYSTIVRA Android screen QA workflow](../.github/workflows/android-screenshots.yml) uses an Android 33 emulator, DEBUG-only route selection and targets 38 actual in-app screenshots (36 original in-app routes plus two added screens). It resizes concept art to the emulator dimensions and runs [visual-diff.py](../tools/visual-diff.py). These comparisons intentionally report differences and **do not** automatically establish 1:1 parity; status and navigation bars, rasterization, fonts and OS density must be reconciled before approval.

Capture artifacts and per-screen diffs must be reviewed, defects fixed, and rerun across multiple physical configurations. The system Splash animation may complete before the screenshot is taken and requires separate verification.

## v0.6 visual inventory

The screenshot script captures 36 original in-app routes and two added screens: Recording Library and Session Insights (38 screenshots total). The original **01-splash** design is deliberately excluded from the regular image-diff set because an Android system Splash is transient and cannot be reliably captured with the same route mechanism. Its visual regression requires a separate start-up recording. Neither of the two added screens has an approved reference rendering yet. A build or screenshot artifact **does not** establish 1:1 parity.

## v0.7 QA incident and recovery

A previous completed screenshot workflow produced screens dominated by Android's "System UI isn't responding" / "Quickstep isn't responding" and first-run immersive tutorial, **not SYSTIVRA**. These captures must not be used in Google Play or counted as 1:1 compliance. The capture script now avoids changing emulator size/density, checks that `com.brendigo.systivra` owns the focused window, fails when a system dialog is foreground, and writes logcat diagnostics. A separate image validation script checks capture count, dimensions, and repeated images before differential analysis. Actual screen alignment remains unverified.

## Real emulator audit — 2026-11-15, CI run 43661286066

Evidence: https://github.com/bren-wp/SYSTIVRA/actions/runs/43661286066

- 38/38 in-app captures existed, were 1080×1920, and passed the distinct-image sanity check.
- 36/36 original in-app routes were compared to reference PNGs. Android system Splash requires its own cold-start evidence.
- **0/37 original reference screens are VERIFIED 1:1.** The capture workflow succeeded as an evidence-collection job, not as pixel-perfect certification.
- Across 36 reference comparisons: average raw RGB MAE **5.984%**; average changed-pixel percentage **83.810%**. Even the lowest changed-pixel comparison is **67.387%**.
- Four priority routes: Dashboard **73.088%** changed pixels (MAE 4.826%); Hardware Explorer **73.539%** (5.290%); Live Monitor **69.393%** (4.567%); Diagnostics Center **73.039%** (4.924%).
- High-deviation routes: About **97.272%**, Settings **96.958%**, Component Test **96.942%** changed pixels.

**Important reference geometry caveat:** the original design SVGs are 390×844 while the physical emulator captures are 1080×1920. The 1080×1920 reference previews retain the 390×844 composition with letterbox margins, not a genuine layout reflow. These raw percentages mix viewport mismatch, system insets, font rasterization, hardware data and actual UI differences; they must never be represented as certified per-component positional errors.

**Confirmed visible defect:** title text inherited black foreground from transparent Compose containers, making top headings unreadable against midnight backgrounds on several routes. `Theme.kt` now provides the intended light foreground through `LocalContentColor`; an instrumented color regression test was added. The first post-fix screenshot run must be compared to this baseline before reporting improvement.

**Google Play screenshot safety:** only genuine emulator evidence is permitted. The screenshot CI no longer auto-commits unreviewed Play images. All eight candidate Play screenshots require human design review; screenshots with readability or layout defects are not approved marketing assets.

Next acceptance stages: (1) re-run actual Android 33 screenshots after theme correction; (2) align viewport/aspect ratio and insets with an approved device baseline without image stretching; (3) fix per-screen typography, cards, icon/illustration placement and scroll states; (4) verify actions, TalkBack and device-specific dynamic readings; (5) authorize a stricter visual regression gate only for independently reviewed, same-viewport baselines.

## PR #20 — native 390 × 844 design-geometry capture (2026-11-15)

The earlier reference comparison used **letterboxed 1080 × 1920 previews of 390 × 844 SVG designs** versus actual 1080 × 1920 Android captures. Its last published result on PR #19 was **36/36 compared, 82.430% average pixels changed and 6.274% RGB MAE**, but that number combines genuine design differences with viewport incompatibility. **It is not a per-component layout accuracy measurement.**

[Native-viewport workflow](../.github/workflows/android-reference-viewport.yml) now:

1. Renders the unaltered `brending/screens/02..37` SVGs at their **original physical 390 × 844 pixel viewport**, using CairoSVG. Design PNGs are labeled as **references**, never real app screenshots.
2. Builds the actual Android APK, boots an Android 33 emulator with KVM, and explicitly configures `wm size 390x844` and `wm density 160`. Checks both emulator overrides rather than silently resizing screenshots in post-processing.
3. Uses the real app's Debug-only `qa_route` launch path to capture 36 in-app reference routes plus two additional real app screens. Validates the **exact 390 × 844 screenshot geometry**, route focus, and unique image content.
4. Compares each unmodified SVG raster with its corresponding genuine app capture using `tools/visual-diff.py`, archiving per-screen absolute RGB errors, changed-pixel percentages, and diff PNGs. These are **diagnostic-only** thresholds (100% allowed), not an acceptance gate or pixel-perfect certificate.
5. Publishes a per-screen Markdown summary and original capture evidence as a GitHub Actions artifact. System Splash remains a separately required cold-launch artifact.

**CI implementation note:** `reactivecircus/android-emulator-runner` invokes its script through `/bin/sh`; Bash-specific `pipefail` must be placed in `tools/capture-reference-viewport.sh` and run via `bash`. The first native-viewport run #43664238079 failed at the shell adapter before app screenshots; a fix was committed and rerun.

### Reference-aligned functional details

A dedicated `ui/ReferenceDetails.kt` now implements the intended design vocabulary for nine screens: CPU Details, GPU Details, Memory Details, Storage Details, Battery Details, Thermal Details, Display Details, Device & Android and Sensor Explorer. Each has the matching two-line heading, tertiary caption, original hero or measured chart block, and four clickable reference rows. A click moves to the real Android reading cards and their sources below the first viewport. Historical chart lines are drawn only if Android supplied at least two numeric percentage samples. Missing data is explicitly labeled; no fake charts or invented CPU/GPU temperatures.

**Current acceptance status: 0/37 originals certified 1:1.** Remaining work includes native-viewport diff review, final typography/insets/icon refinements for every destination, initial system Splash, 360/412/600/800 viewport testing, TalkBack, user-flow tests and real-device confirmation. CI builds, screenshots or matching layout structure **do not by themselves** certify 1:1.


## PR #22 — truthful action details and native viewport auditing (2026-11-15)

After PR #21 was merged, the last verified native-width 390 × 844 emulator comparison
([run #43667371243](https://github.com/bren-wp/SYSTIVRA/actions/runs/43667371243))
had **36/36** non-Splash original reference comparisons, mean absolute RGB error
**5.138%** and mean changed pixels **67.202%**. These are raw color differences
from concept boards, not a pixel-perfect match: **0/37 originals certified 1:1**.

PR #22 preserves the original four-row entrance layout for each hardware detail
route. Below the first viewport, users can view the selected mapped Android
measurement, availability, source label, perform an offline reading search, and
copy a valid reading. Real measured samples may populate the memory percentage
chart; no fabricated chart is substituted for absent device data. History session
selection now targets the actual locally stored session.

The Python visual-QA summary previously carried the *old* 1080 × 1920 letterbox
caveat into native 390 × 844 test reports. The current script takes
`--geometry native` in the [native viewport workflow](../.github/workflows/android-reference-viewport.yml)
so the evidence correctly distinguishes native SVG rasterization from letterboxed
marketing previews. Differences caused by Android OS bars and reference-only
illustrative graphs are still real visual differences; they must not be hidden
or interpreted as a verified 1:1 pass.

**Acceptance pending:** completed APK/AAB lint/unit build, connected emulator UI
tests, fresh full-screen native visual comparison, separate Android Splash
capture, other densities/sizes, actual-device safety checks, and final icon,
font, insets and responsive-layout corrections across all 37 references.


## PR #23 — highest-difference native screens and independent Splash / responsiveness QA

Source baseline: [PR #22 native capture #43766977134](https://github.com/bren-wp/SYSTIVRA/actions/runs/43766977134).
The **36/36** non-Splash original screens were compared at **390×844** physical
pixels, **67.202% mean changed pixels**, and **5.144% mean RGB MAE**. Five
priority routes were:

| Reference | Route | Changed pixels before this iteration |
|---|---|---:|
| 31 Benchmark Results | benchmark-results | 93.676% |
| 20 App Usage | app-usage | 87.874% |
| 24 Session Analysis | session-analysis | 87.390% |
| 30 Benchmark Running | benchmark-running | 83.353% |
| 08 CPU Core Inspector | cpu-cores | 83.171% |

These five routes now use shared original-reference heading, measured-data
chart or SYSTIVRA hero, and four clickable reference rows. True metric
availability, benchmark cancellation, optional app usage permissions, recorded
session analysis and CPU inspection remain reachable in dedicated detail
destinations; no synthetic benchmark progress or per-core measurements.
Green CI does not certify design parity.

### New separate QA workflows

1. [Splash cold start](../.github/workflows/android-splash-cold-start.yml):
   launches a clean Android 33 emulator at 390×844, begins a genuine screen
   recording **before** launching the app, retains the video/temporal frames,
   validates changed frames and archives a contact sheet alongside (but clearly
   separate from) the original 01-Splash SVG render. This does not certify
   1:1 and cannot guarantee capture of an OS animation frame on every device.
2. [Responsive capture](../.github/workflows/android-responsive-qa.yml):
   captures **8 actual routes at each of four viewport sizes** (360×800,
   412×915, 600×960, 800×1280), with a 130% accessibility font-scale
   variation at 412×915, for 32 genuine Android screenshots. It rejects blank,
   wrong-sized and grossly duplicated captures.
3. [Native 390×844 QA](../.github/workflows/android-reference-viewport.yml):
   captures 38 actual in-app destinations and compares the 36 original in-app
   screenshots with the native SVG rasters; it independently runs Android
   instrumented tests, collecting JUnit evidence and Logcat on failure.

All results are *diagnostic*, not approvals. Physical-device checks, color/font
rasterization and touch targets, all 37 reference screens (particularly the
system Splash), multiple OS versions, TalkBack, landscape and insets must be
reviewed before any 1:1 or release-ready claim.
