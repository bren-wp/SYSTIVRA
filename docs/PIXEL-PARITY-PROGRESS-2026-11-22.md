# SYSTIVRA — all-screen pixel-parity implementation follow-up (PR #46)

## Evidence baseline and scope

Source designs: the **37 original SVG boards** under `brending/screens/`, all retained unchanged. Their screenshot geometry is **390x844**. Source `01-splash.svg` is a conceptual system Splash design; the other 36 boards correspond to Android screens.

Actual Android baseline: [merged-main QA run 44973870570](https://github.com/bren-wp/SYSTIVRA/actions/runs/44973870570), artifact `systivra-390x844-actual-vs-original-svg-diff`. On the previous Android 33 / 160 dpi baseline, the 36 in-app comparisons show:

- Average RGB mean absolute error: **2.653%**.
- Average changed pixels: **34.985%**.
- Heading region mean RGB error: **4.889%**.
- Hero/chart region mean RGB error: **1.803%**.
- Cards/buttons region mean RGB error: **2.364%**.
- Bottom navigation region mean RGB error: **5.985%**. Bottom reference band is at y782 while a real Android 3-button navigation zone reserves physical screen space; moving interactive app tabs on top of the OS buttons would violate accessibility and safe-area behavior.

**These are baseline measurements, not results after this PR. No original screen is certified 1:1.** Android status/navigation controls, glyph rasterization, availability-driven hardware data and the SVG's illustrative zigzag cannot be replaced with fake values to manufacture a pass.

## Confirmed and implemented in the open PR

1. **All 14 SVG chart screens** share the same 170dp raster geometry. The 13 previously using `ReferenceMeasuredChart` now have dashed guides at y=286/316/346 rather than y~276/304/331. `LiveMonitor` previously used a different 172dp, gridless chart: it now shares the 170dp geometry but preserves its actual battery sample count and working tools dialog.
2. The first card beneath a 170dp chart is corrected by 1dp to respect the source y=402 action-row anchor. The same shared Compose components propagate this correction to affected routes instead of modifying the artwork.
3. The screenshot workflow now runs **`tools/verify-reference-layout.py`** against the actual 36 native Android captures: title-start alignment within 2 physical pixels, all visible SVG cards aligned within 2 pixels, and chart grid ink at y=286/316/346. Missing captures or incompatible geometry fail the check. This detects geometry defects hidden by global RGB percentages.
4. **Expanded responsive QA**: five separately booted Android 33 emulator configurations (`360x800`, `412x915` at 130% font, `600x960`, `800x1280`, `960x600` landscape-sized), each capturing **36 source routes + Recording Library + Session Insights = 38 real APK screens**, totaling **190 proposed captures**. These are still subject to CI execution and manual screenshot review; screenshots are never resized SVG concepts.
5. System Splash continues to be checked in a separate **actual cold-start video** job. A custom full-screen imitation is not inserted, and no fake clock/battery/sensor values are used to force a visual comparison.

## Verification gate before merge

- Require all four sharded instrumented test groups to finish with JUnit evidence on the exact final PR SHA. Prior failure was an emulator/test hang, not a verified app bug.
- Require build/lint/unit tests, 36-screen screenshot diff, app-owned geometry assertions, five responsive matrices and genuine Splash video on the same SHA. Every failed job remains visible.
- Inspect and approve **each** of the 37 SVG source comparisons, plus the two additional screens, manually; list remaining deviations per route. Current automated constraints detect regressions, not full visual equivalence.
- Verify physical Android devices, 130%+ font scale, TalkBack, button reachability, real device telemetry and dynamic charts. Screenshot checks are a separate criterion from functional/accessibility checks.
- Keep the PR as **Draft** and refrain from production release until these are verified. Developer preview 12 currently supplies an unsigned AAB, not a signed Play Store upload.

## Acceptance reporting standard

A screen may be marked **1:1 verified** only when its actual, unmodified device screenshot is compared against an approved matching viewport reference, OS-controlled differences are itemized without hiding app defects, actionable coordinates and typography are independently reviewed, and its interaction/accessibility tests pass. Else mark **not verified** with concrete remaining differences.
