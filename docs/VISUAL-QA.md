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
