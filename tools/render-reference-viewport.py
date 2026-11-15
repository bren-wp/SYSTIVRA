#!/usr/bin/env python3
"""Rasterize unmodified original 390×844 design SVGs for like-for-like Android QA.

These are DESIGN references only, never genuine device or Google Play captures.
Do not resize 1080×1920 letterboxed concept previews: render source SVG directly.
"""
import argparse
from pathlib import Path

import cairosvg
from PIL import Image

WIDTH, HEIGHT = 390, 844


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source", type=Path, default=Path("brending/screens"))
    parser.add_argument("--output", type=Path, default=Path("qa/reference-viewport/references"))
    args = parser.parse_args()
    sources = sorted(args.source.glob("[0-9][0-9]-*.svg"))
    expected = {f"{index:02d}" for index in range(1, 38)}
    if len(sources) != 37 or {x.name[:2] for x in sources} != expected:
        parser.error("Expected 37 uniquely numbered source SVG references (01–37)")
    args.output.mkdir(parents=True, exist_ok=True)
    for svg in sources:
        # The Android Splash is an OS-mediated cold-start state; it is not an
        # in-app route and requires its own evidence/cold-launch capture.
        if svg.name.startswith("01-"):
            continue
        destination = args.output / (svg.stem + ".png")
        cairosvg.svg2png(
            url=str(svg), write_to=str(destination),
            output_width=WIDTH, output_height=HEIGHT,
        )
        with Image.open(destination) as image:
            if image.size != (WIDTH, HEIGHT):
                raise ValueError(f"Invalid raster geometry: {destination.name}")
    if len(list(args.output.glob("*.png"))) != 36:
        raise ValueError("Expected 36 in-app reference raster images")
    print("Rendered 36 original SVGs at the native 390×844 design viewport.")
    print("These are reference designs, NOT screenshots of the Android app.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
