# SYSTIVRA — original 390×844 SVG geometry contract

This specification comes from the 37 **original** files at `brending/screens/*.svg`,
not from a reimagined layout or a decorative screenshot. The Android
app must implement the same first-viewport composition while preserving
functional data and OS accessibility.

## Coordinate system

- Reference viewport: **390 × 844 px**. GitHub CI forces **160 dpi** for 1dp = 1px.
- Coordinate origin: top-left of the original full-device SVG.
- Original source status clock/indicators are illustrative; the real app must
  show the user's real Android status bar, never an invented status clock.
- Main gradient: `linearGradient`, **(0,0)→(390,844)**, `#0D1F3F→#08111D`.
- Native content insets and Android system navigation icons differ between
  OS versions. Do not hide native controls merely to improve a pixel-diff score.

## Source geometry

| Component | Exact source geometry |
|---|---|
| Status text | x=22, baseline y=35, Arial 13px |
| First heading | x=24, baseline y=114, 30px/800 weight |
| Subtitle | x=24, baseline y=153, 22px/700 weight |
| Caption | x=24, baseline y=179, 13px |
| Hero panel | x=20, y=208, w=350, h=128, radius=18 |
| Hero ring | center (78,272), SVG stroke centerline radius=38, stroke=11 |
| Ring accent | start at (78,234), ends approximately (113,288), ~113° |
| Hero wordmark | x=143, baseline y=262, 18px/700 |
| Hero caption | x=143, baseline y=285, 13px |
| Chart panel | x=20, y=208, w=350, h=170, radius=18 |
| First info/action row (hero screens) | x=20, y=359, w=350, h=69, radius=14 |
| Following hero rows | top y = 441, 523, 605 (82px step) |
| First info/action row (chart screens) | x=20, y=402, w=350, h=69, radius=14 |
| Following chart rows | top y = 484, 566, 648 (82px step) |
| Icon background tile | x=34, row top+17, 34×34, radius=10 |
| Row headline | x=81, first row baseline y=390 or chart row y=433, size16/700 |
| Row description | x=81, first row baseline y=410 or chart row y=453, size12 |
| Chevron | right aligned to x=345, size22 |
| Bottom app tab bar | y=782, h=62, `#0D192B` |
| Bottom tab labels | left edges x=45, 149, 246, 326, 12px |

The source text symbols `◇ ▤ ◉ ⌁` sometimes rasterize as missing-glyph
boxes in SVG preview engines, while Android fonts render proper symbols.
This is a **known glyph mismatch**, not an invitation to substitute broken
icons into the real app. Use a consistent icon set in future revisions and
review any deliberate source-SVG updates separately.

## Verification and nonnegotiable quality gates

- Compare the **original** SVG rasterized at its source viewport with a
  **genuine 390×844 Android screenshot**, without resizing or letterboxing.
- Instrumented Compose geometry tests must verify source positions, ring size,
  row card dimensions and the 82px row pitch; action cards remain fully tappable.
- Capture all **38 actual app routes**; compare the **36 original in-app**
  SVG sources. System Splash is the **37th** original and must be recorded
  independently from a true cold start, before the Activity launches.
- Run separate real emulator capture at **360×800, 412×915 (130% fonts),
  600×960 and 800×1280** and inspect for clipping, touch targets and insets.
- Real hardware testing, TalkBack and manual design signoff are independent
  gates. **Pixel-diff percentages are diagnostic, never 1:1 certifications.**
- Never invent live sensor, CPU/GPU, thermal, benchmark or battery data to make
  a chart resemble a decorative concept SVG.

Current complete visual acceptance: **0/37 source designs independently
certified exact 1:1**. Continue comparing and correcting one source
reference at a time until explicitly approved.
