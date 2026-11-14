#!/usr/bin/env python3
"""Strict SYSTIVRA screenshot comparison. Never claims 1:1 without captured pixels.

Example:
  pip install Pillow
  python tools/visual-diff.py \
      --reference brending/rendered \
      --actual qa/screenshots \
      --output qa/diff

Matching basenames must have the same physical pixel dimensions.
"""
import argparse
import csv
import sys
from pathlib import Path
from PIL import Image, ImageChops, ImageEnhance, ImageStat


def inspect(reference: Path, actual: Path, output: Path) -> tuple[str, float, float, str]:
    with Image.open(reference) as ref_image, Image.open(actual) as actual_image:
        expected = ref_image.convert("RGBA")
        observed = actual_image.convert("RGBA")
        if expected.size != observed.size:
            return reference.name, 100.0, 100.0, (
                "Size mismatch: reference %s vs actual %s" % (expected.size, observed.size)
            )
        diff = ImageChops.difference(expected, observed).convert("RGB")
        stat = ImageStat.Stat(diff)
        mean_absolute_error = sum(stat.mean) / len(stat.mean) / 255.0 * 100.0
        luminance = diff.convert("L")
        pixels_changed = luminance.point(lambda v: 255 if v > 0 else 0)
        total = pixels_changed.width * pixels_changed.height
        fraction_changed = (sum(pixels_changed.histogram()[1:]) / total * 100.0) if total else 0.0
        output.mkdir(parents=True, exist_ok=True)
        ImageEnhance.Contrast(diff).enhance(3.0).save(output / reference.name)
        return reference.name, mean_absolute_error, fraction_changed, "Compared"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--reference", type=Path, required=True)
    parser.add_argument("--actual", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--max-mae-percent", type=float, default=0.0)
    parser.add_argument("--max-changed-percent", type=float, default=0.0)
    args = parser.parse_args()

    originals = sorted(x for x in args.reference.glob("*.png")
                       if x.name[:2].isdigit())
    if not originals:
        print("ERROR: no reference PNG screens found", file=sys.stderr)
        return 2
    if not args.actual.is_dir():
        print("ERROR: actual Android screenshot directory not found", file=sys.stderr)
        return 2

    rows = []
    missing = 0
    failing = 0
    for reference in originals:
        capture = args.actual / reference.name
        if not capture.is_file():
            missing += 1
            rows.append((reference.name, "", "", "Missing actual screenshot"))
            continue
        name, mae, changed, message = inspect(reference, capture, args.output)
        rows.append((name, "%.6f" % mae, "%.6f" % changed, message))
        if message != "Compared":
            failing += 1
        elif mae > args.max_mae_percent or changed > args.max_changed_percent:
            failing += 1

    args.output.mkdir(parents=True, exist_ok=True)
    with (args.output / "report.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.writer(stream)
        writer.writerow(["screen", "mean_abs_error_percent", "changed_pixels_percent", "status"])
        writer.writerows(rows)
    print("SYSTIVRA visual comparison: %d references, %d missing, %d failed." %
          (len(originals), missing, failing))
    print("Report:", args.output / "report.csv")
    return 1 if (missing or failing) else 0


if __name__ == "__main__":
    raise SystemExit(main())
