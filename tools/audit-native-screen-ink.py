#!/usr/bin/env python3
"""Audit visible native Android title/subtitle/caption ink in 36 SVG routes.

Reads real, unretouched 390x844 emulator PNGs. The test prevents a global
dark-on-dark regression in the shared typography for every original in-app
reference screen. It does NOT prove visual parity, every off-screen control,
or accessibility of all Android OEM rendered states. Those need Compose
semantic tests, interaction captures and physical-device review.
"""
from __future__ import annotations

import argparse
import csv
from pathlib import Path
from PIL import Image

REGIONS = {
    "headline": ((24, 87, 366, 125), 175, 500),
    "subtitle": ((24, 132, 366, 160), 175, 500),
    "caption": ((24, 163, 366, 189), 95, 100),
}

def bright_pixels(image: Image.Image, rect: tuple[int,int,int,int], floor: int) -> int:
    # A pixel counts only if all RGB channels have meaningful luminance.
    return sum(1 for r,g,b in image.crop(rect).convert("RGB").getdata()
               if min(r,g,b) >= floor)

def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--screenshots",type=Path,required=True)
    parser.add_argument("--output",type=Path,required=True)
    args=parser.parse_args()
    names = sorted(x for x in args.screenshots.glob("*.png")
                   if x.name[:2].isdigit() and 2 <= int(x.name[:2]) <= 37)
    if len(names) != 36 or {int(x.name[:2]) for x in names} != set(range(2,38)):
        raise SystemExit("Expected all 36 original non-Splash reference-route PNG captures")
    args.output.mkdir(parents=True, exist_ok=True)
    rows=[]
    failures=[]
    for path in names:
        with Image.open(path) as im:
            if im.size != (390,844):
                failures.append(path.name + ": wrong physical viewport")
                continue
            for label,(rect,floor,minimum) in REGIONS.items():
                found=bright_pixels(im,rect,floor)
                rows.append((path.name,label,found,minimum))
                if found < minimum:
                    failures.append(path.name + " "+label + " has only " + str(found) +
                                    " visible ink pixels (expected >= " + str(minimum) + ")")
    with (args.output/"native-screen-ink.csv").open("w",newline="",encoding="utf-8") as f:
        w=csv.writer(f)
        w.writerow(["screen","text_region","visible_ink_pixels","minimum_required"])
        w.writerows(rows)
    lines=[
        "# SYSTIVRA native dark-theme text-ink visibility QA","",
        "Checks all 36 original in-app 390×844 native screenshot headings, subtitles, and captions.",
        "Screenshots are untouched. **Passing is NOT pixel-perfect certification.**","",
        "| Region | Minimum ink pixels across 36 screens | Required |",
        "|---|---:|---:|"
    ]
    for label in REGIONS:
        subset=[x[2] for x in rows if x[1]==label]
        if subset:
            lines.append("| "+label+" | "+str(min(subset))+" | "+
                         str(REGIONS[label][2])+" |")
    lines += ["", "## Failures", ""]
    lines += ["- "+x for x in failures] if failures else ["- None within the screened text regions"]
    lines += ["", "This tests only screen-wide **first viewport** typography; dialog,",
              "chip, text field, button and disabled/selected foregrounds are",
              "verified separately by Kotlin color-role and instrumented UI tests."]
    (args.output/"native-screen-ink.md").write_text("\n".join(lines)+"\n",encoding="utf-8")
    print("\n".join(lines))
    if failures: raise SystemExit("Dark-on-dark typography regression: " + "; ".join(failures))

if __name__ == "__main__":
    main()
