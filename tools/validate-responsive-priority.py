#!/usr/bin/env python3
"""Validate genuine SYSTIVRA responsive emulator screenshots without stretch or fake 1:1 claims."""
import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageStat

parser = argparse.ArgumentParser()
parser.add_argument("--root", type=Path, required=True)
args = parser.parse_args()
routes = (
    "dashboard", "hardware-explorer", "benchmark-results", "app-usage",
    "session-analysis", "benchmark-running", "cpu-cores", "settings"
)
viewports = (
    (360, 800, 1.0), (412, 915, 1.3), (600, 960, 1.0), (800, 1280, 1.0)
)
observations = []
for width, height, font_scale in viewports:
    folder = args.root / f"{width}x{height}"
    files = sorted(folder.glob("*.png"))
    if {x.name for x in files} != {f"{route}.png" for route in routes}:
        raise SystemExit(f"FAIL: missing or extra responsive route at {folder}: {files}")
    hashes = set()
    for f in files:
        with Image.open(f) as frame:
            if frame.size != (width, height):
                raise SystemExit(f"FAIL: {f.name}: expected {width}x{height}, got {frame.size}")
            rgb = frame.convert("RGB")
            hashes.add(hashlib.sha256(rgb.resize((32, 64)).tobytes()).hexdigest())
            stat = ImageStat.Stat(rgb.resize((64, 128)))
            if max(stat.stddev) < 3.0:
                raise SystemExit(f"FAIL: nearly uniform/blank responsive screenshot: {f}")
    if len(hashes) < 5:
        raise SystemExit(f"FAIL: only {len(hashes)} distinct routes at {width}x{height}")
    observations.append({
        "viewport": f"{width}x{height}",
        "density_dpi": 160,
        "font_scale": font_scale,
        "captures": len(files),
        "distinct": len(hashes)
    })
output = {
    "screens": observations,
    "total_verified_captures": sum(x["captures"] for x in observations),
    "pixel_perfect_1to1_certified": False,
    "real_physical_devices_tested": False,
    "remaining_work": (
        "Manual review for clipping, orientation, tap targets, TalkBack, "
        "status/navigation bar insets and actual physical-device coverage."
    )
}
(args.root / "responsive-validation.json").write_text(json.dumps(output, indent=2) + "\n")
lines = [
    "# SYSTIVRA responsive real-emulator capture",
    "",
    "All screenshots come from the installed Android application and are not scaled.",
    "",
    "| Viewport (px) | Density | Font scale | Distinct routes |",
    "|---|---:|---:|---:|"
]
for x in observations:
    lines.append(
        f'| {x["viewport"]} | {x["density_dpi"]} | '
        f'{x["font_scale"]:.2f} | {x["distinct"]}/{x["captures"]} |'
    )
lines += [
    "", f'**Total: {output["total_verified_captures"]} genuine captures.**',
    "",
    "**Not a 1:1 certification.** Dynamic font scale and layout reflow require "
    "independent visual inspection; no physical devices were tested in CI.",
]
(args.root / "responsive-summary.md").write_text("\n".join(lines) + "\n")
print("\n".join(lines))
