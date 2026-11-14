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
