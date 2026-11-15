#!/usr/bin/env python3
"""Validate authentic Android cold-start video frames; never certify 1:1 from a video alone."""
import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps

parser = argparse.ArgumentParser()
parser.add_argument("--frames", type=Path, required=True)
parser.add_argument("--output", type=Path, required=True)
parser.add_argument("--contact-sheet", type=Path, required=True)
args = parser.parse_args()

frames = sorted(args.frames.glob("cold-*.png"))
if len(frames) < 10:
    raise SystemExit(f"FAIL: no continuous cold-start video, found only {len(frames)} frames")
fingerprints = set()
sizes = set()
for path in frames:
    with Image.open(path) as frame:
        sizes.add(frame.size)
        fingerprints.add(hashlib.sha256(
            frame.convert("RGB").resize((32, 64)).tobytes()
        ).hexdigest())
if any(width < 320 or height < 600 for width, height in sizes):
    raise SystemExit(f"FAIL: invalid Android frame geometry: {sizes}")
if len(fingerprints) < 2:
    raise SystemExit("FAIL: no visible transition from pre-launch state to application")
# A contact sheet is only evidence for manual Splash review, NOT an approved screenshot.
sheet = Image.new("RGB", (4 * 195, 4 * 442), "#08111D")
draw = ImageDraw.Draw(sheet)
for i, path in enumerate(frames[:16]):
    with Image.open(path) as im:
        thumb = ImageOps.contain(im.convert("RGB"), (195, 422))
        col, row = i % 4, i // 4
        sheet.paste(thumb, (col * 195, row * 442 + 18))
        draw.text((col * 195 + 8, row * 442 + 2), f"Frame {i:02}", fill="white")
args.contact_sheet.parent.mkdir(parents=True, exist_ok=True)
sheet.save(args.contact_sheet)
report = {
    "captured_frames": len(frames),
    "unique_frames": len(fingerprints),
    "video_frame_dimensions": sorted([list(x) for x in sizes]),
    "original_svg_1to1_certified": False,
    "limitations": [
        "Video frames document a real cold start, not an exact 1:1 visual match.",
        "Manual review must confirm launcher icon, duration and splash-to-Dashboard transition.",
        "Android system Splash timing is OS/device-dependent."
    ],
}
args.output.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
print(json.dumps(report, indent=2))
