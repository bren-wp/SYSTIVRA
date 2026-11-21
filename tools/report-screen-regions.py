#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Compare actual Android pixels with original SVG in app-owned UI regions.

This intentionally does NOT discard OS bars from the raw pixel-diff report.
Per-region measurements reveal typography/cards/gloss defects instead of
confusing them with status/nav rendering. Never call a screen 1:1 based on this
analysis without interactive QA and independent manual sign-off.
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path
from statistics import mean

from PIL import Image, ImageChops, ImageStat

# Coordinates defined against original unscaled 390x844 SVG viewport.
REGIONS = {
    "heading": (20, 80, 370, 194),
    "hero_or_chart": (20, 208, 370, 379),
    "cards_and_buttons": (20, 359, 370, 720),
    "bottom_tab_bar": (0, 782, 390, 844),
}
NUMERIC_SOURCE_SCREENS = {f"{i:02}" for i in range(2, 38)}


def measure(ref: Image.Image, actual: Image.Image, box: tuple[int, int, int, int]) -> tuple[float, float]:
    difference = ImageChops.difference(ref.crop(box), actual.crop(box)).convert("RGB")
    mae = mean(ImageStat.Stat(difference).mean) / 255.0 * 100.0
    changed = sum(1 for px in difference.getdata() if any(channel > 0 for channel in px))
    return mae, changed * 100.0 / (difference.width * difference.height)


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--reference", type=Path, required=True)
    p.add_argument("--actual", type=Path, required=True)
    p.add_argument("--output", type=Path, required=True)
    args = p.parse_args()

    ref_names = sorted(x.name for x in args.reference.glob("*.png") if x.name[:2].isdigit())
    if len(ref_names) != 36 or {x[:2] for x in ref_names} != NUMERIC_SOURCE_SCREENS:
        raise SystemExit("Expected the original 36 unique non-Splash SVG reference PNGs (02–37)")
    rows = []
    for name in ref_names:
        target = args.actual / name
        if not target.is_file():
            raise SystemExit("Missing actual emulator screenshot: " + str(target))
        with Image.open(args.reference / name) as r, Image.open(target) as a:
            if r.size != (390, 844) or a.size != r.size:
                raise SystemExit("Native screenshots must be 390x844: " + name)
            ref = r.convert("RGB")
            actual = a.convert("RGB")
            for key, rect in REGIONS.items():
                mae, changed = measure(ref, actual, rect)
                rows.append((name, key, mae, changed))

    args.output.mkdir(parents=True, exist_ok=True)
    with (args.output / "regions.csv").open("w", encoding="utf-8", newline="") as out:
        writer = csv.writer(out)
        writer.writerow(("screen", "region", "rgb_mae_percent", "changed_pixels_percent"))
        for name, key, mae, changed in rows:
            writer.writerow((name, key, f"{mae:.6f}", f"{changed:.6f}"))

    lines = [
        "# SYSTIVRA: real Android region-by-region visual QA",
        "",
        "**Diagnostic, not 1:1 certification.** Native 390x844 screenshot comparisons; 36 original in-app routes.",
        "No input images are modified or masked. Android system bars remain in the separate full-screen diff.",
        "",
        "| UI region | Mean RGB MAE | Mean changed pixels |",
        "|---|---:|---:|",
    ]
    for region in REGIONS:
        values = [(mae, changed) for _name, key, mae, changed in rows if key == region]
        lines.append(f"| {region} | {mean(x[0] for x in values):.3f}% | {mean(x[1] for x in values):.3f}% |")
    lines += [
        "",
        "## Largest per-screen geometry/color mismatches by region",
        "",
        "| Screen | Region | RGB MAE | Changed pixels |",
        "|---|---|---:|---:|",
    ]
    for name, region, mae, changed in sorted(rows, key=lambda x: x[2], reverse=True)[:18]:
        lines.append(f"| {name} | {region} | {mae:.3f}% | {changed:.3f}% |")
    lines.append("")
    (args.output / "regions.md").write_text("\n".join(lines), encoding="utf-8")
    print("\n".join(lines))


if __name__ == "__main__":
    main()
