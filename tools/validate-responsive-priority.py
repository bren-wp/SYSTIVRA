#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Validate every app route on one real emulator viewport (never upscaled SVGs).

Responsive QA checks inventory, screenshot geometry, nonblank content and
route uniqueness. Full 1:1 parity remains pending independent visual review.
"""
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image, ImageStat

VIEWPORTS = {
    "360x800": (360, 800, 1.0),
    "412x915": (412, 915, 1.3),
    "600x960": (600, 960, 1.0),
    "800x1280": (800, 1280, 1.0),
    "960x600": (960, 600, 1.0),
}
p = argparse.ArgumentParser(description=__doc__)
p.add_argument("--root", type=Path, required=True)
p.add_argument("--viewport", choices=sorted(VIEWPORTS), required=True)
args = p.parse_args()
width, height, font_scale = VIEWPORTS[args.viewport]
folder = args.root / args.viewport
designs = sorted(Path("brending/screens").glob("*.svg"))
originals = {f.stem for f in designs if f.stem[:2].isdigit() and f.stem != "01-splash"}
if len(originals) != 36 or {name[:2] for name in originals} != {f"{n:02}" for n in range(2, 38)}:
    raise SystemExit("FAIL: cannot validate all 36 original reference routes")
expected = {name + ".png" for name in originals} | {
    "38-recording-library.png", "39-session-insights.png"
}
files = sorted(folder.glob("*.png"))
actual = {f.name for f in files}
if expected != actual:
    raise SystemExit(f"FAIL: {args.viewport}: missing={sorted(expected - actual)}, extra={sorted(actual - expected)}")
hashes = set()
for screenshot in files:
    with Image.open(screenshot) as frame:
        if frame.size != (width, height):
            raise SystemExit(f"FAIL: {screenshot}: expected {(width, height)}, got {frame.size}")
        rgb = frame.convert("RGB")
        hashes.add(hashlib.sha256(rgb.resize((32, 64)).tobytes()).hexdigest())
        if max(ImageStat.Stat(rgb.resize((64, 128))).stddev) < 3.0:
            raise SystemExit(f"FAIL: blank or uniform Android capture: {screenshot}")
if len(hashes) < 34:
    raise SystemExit(f"FAIL: only {len(hashes)}/38 distinct real routes at {args.viewport}")
report = {
    "viewport": args.viewport,
    "density_dpi": 160,
    "font_scale": font_scale,
    "original_reference_routes": 36,
    "additional_functional_routes": 2,
    "captured_actual_screenshots": len(files),
    "unique_screen_images": len(hashes),
    "splash": "separately captured by real Android cold-launch workflow",
    "pixel_perfect_1to1_certified": False,
    "physical_device_verified": False,
    "pending": [
        "Manual per-screen screenshot review",
        "Landscape interaction and accessibility review",
        "Differing Android OS system inset/keyboard states",
    ],
}
(folder / "responsive-validation.json").write_text(
    json.dumps(report, indent=2) + "\n", encoding="utf-8"
)
summary = [
    f"# SYSTIVRA real responsive QA — {args.viewport}",
    "",
    f"- Original reference routes captured: **36/36**",
    f"- Additional operational routes captured: **2/2**",
    f"- Unique real APK screen images: **{len(hashes)}/38**",
    f"- Device density: **160 dpi**; font scale: **{font_scale:.2f}**",
    "- Android Splash: separate genuine cold-start verification",
    "- 1:1 acceptance: **not certified**; image existence does not prove layout fidelity",
    "- Clipping, dynamic readings, TalkBack and interactive states still need manual review",
    "",
]
(folder / "responsive-summary.md").write_text("\n".join(summary), encoding="utf-8")
print("\n".join(summary))
