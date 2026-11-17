# SYSTIVRA — Google Play artwork

Official editable vector sources: [source/](source/). Generated, precisely sized PNGs: [assets/](assets/).

**Store-ready graphics only**
- `assets/icon-512.png` — 512 × 512, 32-bit PNG (RGBA), under 1 MB.
- `assets/feature-graphic-1024x500.png` — 1024 × 500, 24-bit PNG (RGB).
- `assets/SYSTIVRA-Play-Graphics.zip` — artwork package, with clear differentiation of design concepts.

**Not ready for Play screenshots:** [concepts-NOT-FOR-PLAY/](concepts-NOT-FOR-PLAY/) contains eight 1080 × 1920 illustrated marketing drafts. These are not images captured from the running application and **must not be uploaded as Google Play screenshots**. Google Play requires authentic in-app captures. Earlier emulator runs suffered Android System UI/Quickstep ANRs, so those screenshots are invalid and deliberately excluded.

**To generate**: `python -m pip install pillow cairosvg`, then `python tools/render_google_play.py`. The GitHub workflow regenerates and validates graphics when editable source changes.

Before publishing, capture at least 2 actual phone screenshots (4 at 1080 × 1920 recommended), validate the privacy policy/Data Safety form, review optional Usage Access disclosure, test signing and ensure Play listing claims match functionality.

**Brand**: midnight navy, cyan, blue, purple, mint; English only, SYSTIVRA by BRENDIGO.

## Real screenshots upload gate

The [Android screen QA workflow](../../.github/workflows/android-screenshots.yml) checks that SYSTIVRA owns the focused window to reject Quickstep/System UI ANR screenshots. Eight **actual emulator captures are already checked into** [real-screenshots/](real-screenshots/) from [this capture commit](https://github.com/bren-wp/SYSTIVRA/commit/2689a7bd495ba4f3fb4d6f3dca68a0cd3605c972). They appear in the [main README gallery](../../README.md#-real-android-application-screenshots), not as substitute concept drawings. All eight remain **pending final human visual review**, and may reflect an earlier APK. They must not be considered approved Play assets or certified 1:1 screens. The [capture integrity check](../../tools/check-readme-captures.py) confirms image paths, nonduplicate PNG bytes and dimensions but does not verify app focus; that requires a fresh capture run and visual inspection. Never upload design concepts as app screenshots.

## Original 37 SYSTIVRA design screens at 1080 × 1920

- [All references as ZIP](reference-previews-NOT-PLAY-SCREENSHOTS/SYSTIVRA-37-REFERENCE-PREVIEWS-1080x1920.zip)
- [One image containing all 37 references](reference-previews-NOT-PLAY-SCREENSHOTS/ALL-37-REFERENCES-contact-sheet.png)
- [Manifest with source hashes](reference-previews-NOT-PLAY-SCREENSHOTS/manifest.json)

These are pixel-preserved **concept references**, resized by an automated non-cropping letterbox process; they are not screenshots of the running Android app and must not be used as official Play phone screenshots. For actual Play screenshots use approved `real-screenshots/` captured and reviewed on devices.
