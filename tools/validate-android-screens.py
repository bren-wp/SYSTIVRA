#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Fail image capture validation if screenshots are malformed or near-duplicates.

This does not certify pixel-perfect parity with a design reference.
Focus/ANR detection is performed by capture-android-screens.sh before capture.
"""
from pathlib import Path
from PIL import Image, ImageStat
import argparse
import hashlib
import json

parser = argparse.ArgumentParser()
parser.add_argument("folder", type=Path)
parser.add_argument("--width", type=int, help="Require exact screenshot width when matching SVG viewport")
parser.add_argument("--height", type=int, help="Require exact screenshot height when matching SVG viewport")
args = parser.parse_args()
if (args.width is None) != (args.height is None):
    parser.error("--width and --height must be passed together")
images = sorted(args.folder.glob("*.png"))
if len(images) < 36:
    raise SystemExit(f"FAIL: expected at least 36 different routed app screenshots, found {len(images)}")
observations = []
fingerprints = set()
for path in images:
    with Image.open(path) as im:
        if im.width < 320 or im.height < 320:
            raise SystemExit(f"FAIL: invalid screen geometry: {path.name} = {im.size}")
        if args.width is not None and im.size != (args.width, args.height):
            raise SystemExit(f"FAIL: expected exact {args.width}x{args.height} real capture, "
                             f"got {path.name} = {im.size}")
        rgb = im.convert("RGB")
        small = rgb.resize((32, 64))
        digest = hashlib.sha256(small.tobytes()).hexdigest()
        fingerprints.add(digest)
        stat = ImageStat.Stat(rgb.resize((64, 128)))
        observations.append({
            "name": path.name, "width": im.width, "height": im.height,
            "digest": digest[:16], "mean_rgb": [round(x, 1) for x in stat.mean]
        })
# Consecutive identical UI images are possible, but the entire screen catalog
# should not consist of repeated Quickstep/System UI dialogs.
if len(fingerprints) < 13:
    raise SystemExit(
        f"FAIL: only {len(fingerprints)} distinct images among {len(images)} captures; "
        "possible system UI ANRs or navigation failures"
    )
(args.folder.parent / "screen-validation.json").write_text(
    json.dumps({"count": len(images), "unique": len(fingerprints), "screens": observations}, indent=2)
)
print(f"Screenshot capture sanity: {len(images)} images, {len(fingerprints)} unique. "
      "Pixel-perfect compliance NOT certified.")
