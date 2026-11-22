#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Assert original SVG-derived geometry against 36 genuine Android captures.

This is a strict *app-owned anchor* regression check, not a claim of full 1:1
pixels. Status and navigation chrome are owned by Android. Dynamic readings
and decorative concept chart paths cannot be substituted for real data.
"""
import argparse
from pathlib import Path
from PIL import Image

PANEL = (21, 34, 53)
CHART_GRID = (36, 66, 92)


def panel_at(image: Image.Image, x: int, y: int) -> bool:
    pixel = image.getpixel((x, y))
    return all(abs(a - b) <= 3 for a, b in zip(pixel, PANEL))


def first_panel(image: Image.Image, expected_y: int) -> int | None:
    return next(
        (y for y in range(expected_y - 4, expected_y + 5)
         if panel_at(image, 355, y)),
        None,
    )


def heading_start(image: Image.Image) -> tuple[int, int] | None:
    matches = [
        (x, y) for y in range(83, 126) for x in range(20, 350)
        if min(image.getpixel((x, y))) >= 210
    ]
    return (min(x for x, _ in matches), min(y for _, y in matches)) if matches else None


def grid_pixel(rgb: tuple[int, int, int]) -> bool:
    # Tolerate Android's 1dp stroke antialiasing; never match the plain panel.
    r, g, b = rgb
    return 25 <= r <= 90 and 44 <= g <= 105 and 59 <= b <= 125


def guide_ink(image: Image.Image, y: int) -> int:
    return max(
        sum(1 for x in range(55, 336) if grid_pixel(image.getpixel((x, yy))))
        for yy in range(y - 1, y + 2)
    )


def audit(reference: Path, actual: Path) -> tuple[int, list[str]]:
    names = sorted(p.name for p in reference.glob("*.png") if p.name[:2].isdigit())
    if len(names) != 36 or {n[:2] for n in names} != {f"{i:02}" for i in range(2, 38)}:
        raise ValueError("expected all 36 original non-Splash PNG references (02-37)")
    failures: list[str] = []
    inspected = 0
    for name in names:
        if not (actual / name).is_file():
            failures.append(f"{name}: missing actual Android screenshot")
            continue
        with Image.open(reference / name) as before, Image.open(actual / name) as after:
            ref, capture = before.convert("RGB"), after.convert("RGB")
            if ref.size != (390, 844) or capture.size != (390, 844):
                failures.append(f"{name}: invalid unscaled screenshot dimensions")
                continue
            is_chart = panel_at(ref, 355, 340)
            ref_title = heading_start(ref)
            app_title = heading_start(capture)
            if ref_title is None or app_title is None:
                failures.append(f"{name}: missing app heading glyphs")
            elif max(abs(ref_title[0] - app_title[0]), abs(ref_title[1] - app_title[1])) > 2:
                failures.append(f"{name}: heading start {app_title}, reference {ref_title}")
            anchors = [208, 402, 484, 566, 648] if is_chart else [208, 359, 441, 523, 605]
            for y in anchors:
                expected = first_panel(ref, y)
                if expected is None:
                    continue  # Some approved screens intentionally have fewer action rows.
                measured = first_panel(capture, y)
                if measured is None or abs(expected - measured) > 2:
                    failures.append(f"{name}: card y~{y}, source {expected}, app {measured}")
            if is_chart:
                # The source SVG guides are at absolute y=286/316/346 in
                # the 390x844 reference, regardless of dynamic actual samples.
                for y in (286, 316, 346):
                    if guide_ink(capture, y) < 12:
                        failures.append(f"{name}: missing/misaligned measured-chart grid at y={y}")
            inspected += 1
    return inspected, failures


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    args = parser.parse_args()
    try:
        inspected, failures = audit(args.reference, args.actual)
    except (ValueError, OSError) as error:
        parser.exit(2, f"ERROR: {error}\n")
    print(f"Checked reference geometry on {inspected}/36 native Android screens.")
    print("The real OS system bars and illustrative paths are NOT 1:1 certified.")
    for problem in failures:
        print("FAIL: " + problem)
    if failures or inspected != 36:
        print(f"FAIL: {len(failures)} per-screen geometry deviations or missing captures.")
        return 1
    print("PASS: headings, visible reference cards and genuine chart grids respect source coordinates.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
