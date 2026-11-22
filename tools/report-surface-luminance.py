#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Measure whether native UI surfaces are darker than their ORIGINAL SVGs.

This is a non-destructive diagnostic for 390x844 Android screenshots. It keeps
Android status/gesture bars in the separate full-screen pixel difference and
does not claim pixel-perfect parity when a comparison is aligned.
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path
from statistics import mean

from PIL import Image, ImageChops, ImageStat

VIEWPORT = (390, 844)
REGIONS = {
    "heading": (20, 80, 370, 194),
    "hero_or_chart": (20, 208, 370, 379),
    "cards_and_buttons": (20, 359, 370, 720),
}
NAV_COLOR = (13, 25, 43)
REFERENCE_NAV = (0, 782, 390, 844)


def signed_luminance(reference: Image.Image, actual: Image.Image, box: tuple[int, ...]) -> float:
    """Positive means lighter than SVG; negative means darker than SVG."""
    ref_l = ImageStat.Stat(reference.crop(box).convert("L")).mean[0]
    native_l = ImageStat.Stat(actual.crop(box).convert("L")).mean[0]
    return native_l - ref_l


def detect_native_tab_strip(actual: Image.Image) -> tuple[int, int, int, int] | None:
    """Recognize the app-owned 62px strip, not Android's separate OS bar.

    Only accept an isolated, near-exact 62px continuous background segment.
    If the OS uses the same color or the screen geometry is ambiguous, return
    None rather than generating an artificially reassuring comparison.
    """
    w, h = actual.size
    if (w, h) != VIEWPORT:
        return None
    px = actual.load()
    runs: list[tuple[int, int]] = []
    start: int | None = None
    for y in range(max(0, h - 180), h):
        left, right = px[1, y], px[w - 2, y]
        matching = all(
            abs(channel - target) <= 1
            for sample in (left, right)
            for channel, target in zip(sample[:3], NAV_COLOR)
        )
        if matching and start is None:
            start = y
        if not matching and start is not None:
            runs.append((start, y))
            start = None
    if start is not None:
        runs.append((start, h))
    candidates = [(a, b) for a, b in runs if 60 <= b - a <= 64]
    if len(candidates) != 1:
        return None
    start, end = candidates[0]
    if start < 0 or start + 62 > h:
        return None
    # Never relocate or mask any screenshots, and preserve the native OS gutter.
    return (0, start, w, start + 62)


def nav_pixel_diff(reference: Image.Image, actual: Image.Image, native_box: tuple[int, ...]) -> tuple[float, float]:
    ref = reference.crop(REFERENCE_NAV).convert("RGB")
    native = actual.crop(native_box).convert("RGB")
    difference = ImageChops.difference(ref, native)
    mae = mean(ImageStat.Stat(difference).mean) / 255 * 100
    changed = sum(1 for pixel in difference.getdata() if any(x > 0 for x in pixel))
    return mae, changed * 100 / (ref.width * ref.height)


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--reference", required=True, type=Path)
    ap.add_argument("--actual", required=True, type=Path)
    ap.add_argument("--output", required=True, type=Path)
    args = ap.parse_args()
    expected_names = [f"{n:02}" for n in range(2, 38)]
    files = sorted(p.name for p in args.reference.glob("*.png") if p.name[:2].isdigit())
    if len(files) != 36 or [p[:2] for p in files] != expected_names:
        raise SystemExit("Expected all 36 original non-Splash SVG screenshot reference PNGs")
    args.output.mkdir(parents=True, exist_ok=True)
    rows: list[tuple[str, str, float]] = []
    aligned: list[tuple[str, int, float, float]] = []
    missing: list[str] = []
    for filename in files:
        actual_path = args.actual / filename
        if not actual_path.is_file():
            raise SystemExit(f"Missing actual Android screenshot: {actual_path}")
        with Image.open(args.reference / filename) as src, Image.open(actual_path) as cap:
            if src.size != VIEWPORT or cap.size != VIEWPORT:
                raise SystemExit(f"Incorrect screenshot dimensions for {filename}")
            reference, native = src.convert("RGB"), cap.convert("RGB")
            for region, box in REGIONS.items():
                rows.append((filename, region, signed_luminance(reference, native, box)))
            box = detect_native_tab_strip(native)
            if box is None:
                missing.append(filename)
            else:
                mae, changed = nav_pixel_diff(reference, native, box)
                aligned.append((filename, box[1], mae, changed))

    with (args.output / "surface-luminance.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["screen", "region", "android_minus_original_svg_mean_luma_0_to_255"])
        for filename, region, delta in rows:
            writer.writerow([filename, region, f"{delta:.3f}"])

    lines = [
        "# Native SYSTIVRA color and Android navigation ownership audit", "",
        "**Diagnostic only — 0/37 screens independently certified pixel-perfect.**",
        "Signed luma delta: negative means Android is darker than the exact original SVG.",
        "Reference and real Android screenshots are neither edited nor color-adjusted.", "",
        "| Region | Mean brightness delta (0–255) | Darker screens / 36 |",
        "|---|---:|---:|",
    ]
    for region in REGIONS:
        values = [delta for _, name, delta in rows if name == region]
        lines.append(f"| {region} | {mean(values):+.2f} | {sum(v < -2 for v in values)} |")
    lines += [
        "", "## 62px application tab strip versus original SVG (OS bar reported separately)",
        "Android's system navigation area is **not** the app tab strip.",
        "Only an unambiguous contiguous 62px app-owned strip is compared. If not",
        "detected, the diagnostic refuses to invent a normalized position.", "",
        f"Detected {len(aligned)}/36 app tab strips; {len(missing)} ambiguous/unavailable.", "",
        "| Screen | App bar top Y in actual capture | App-only RGB MAE | App-only changed pixels |",
        "|---|---:|---:|---:|",
    ]
    for filename, top, mae, changed in aligned:
        lines.append(f"| {filename} | {top} | {mae:.3f}% | {changed:.3f}% |")
    if missing:
        lines += ["", "Not safely detected: " + ", ".join(missing)]
    lines += [
        "", "The unmodified whole-screen screenshot comparison remains authoritative",
        "for literal whole-screen differences; this report helps distinguish native",
        "Android OS navigation geometry from controllable SYSTIVRA colors and labels.",
        "Passing any QA workflow does **not** mean these numbers reached 1:1.", "",
    ]
    output_path = args.output / "surface-luminance.md"
    output_path.write_text("\n".join(lines), encoding="utf-8")
    print("\n".join(lines))


if __name__ == "__main__":
    main()
