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

The [SYSTIVRA Android screen QA workflow](../.github/workflows/android-screenshots.yml) uses an Android 35 emulator, DEBUG-only route selection and saves 37 actual emulator screenshots. It resizes concept art to the emulator dimensions and runs [visual-diff.py](../tools/visual-diff.py). These comparisons intentionally report differences and **do not** automatically establish 1:1 parity; status and navigation bars, rasterization, fonts and OS density must be reconciled before approval.

Capture artifacts and per-screen diffs must be reviewed, defects fixed, and rerun across multiple physical configurations. The system Splash animation may complete before the screenshot is taken and requires separate verification.
