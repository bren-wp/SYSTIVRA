#!/usr/bin/env python3
"""Measure original SVG vs real Android text glyph top positions at 390x844.

Only reads screenshots; it does not manipulate, mask, or retouch reference
images or count native system UI as part of the app. This evidence is diagnostic
and does NOT imply any screen is independently certified pixel-perfect.
"""
from __future__ import annotations

import argparse
from pathlib import Path
from PIL import Image

SCREENS = ("02-system-health", "03-device-detection", "04-privacy-center", "05-dashboard")
# Regions deliberately exclude icons and the dynamic phone status/navigation bars.
# Reference text is #F4F8FC (white) or #9CAFBE (muted) in all these screens.
REGIONS = (
    ("heading title",   (24,  80, 350, 128), "white"),
    ("heading subtitle",(24, 126, 350, 166), "white"),
    ("heading caption", (24, 162, 350, 195), "muted"),
    ("hero title",      (143,236, 345, 271), "white"),
    ("hero subtitle",   (143,270, 345, 303), "muted"),
    ("row 1 title",     (81, 366, 328, 399), "white"),
    ("row 1 caption",   (81, 398, 328, 420), "muted"),
)

def start_y(im: Image.Image, region: tuple[int,int,int,int], tone: str) -> int | None:
    x0,y0,x1,y1 = region
    rgb = im.convert("RGB")
    result = []
    for y in range(y0,y1):
        count = 0
        for x in range(x0,x1):
            r,g,b = rgb.getpixel((x,y))
            if tone == "white":
                eligible = r >= 210 and g >= 210 and b >= 210
            else:
                eligible = abs(r-156) < 52 and abs(g-175) < 52 and abs(b-190) < 52
                eligible = eligible and (r + g + b > 350)
            if eligible:
                count += 1
        if count >= 4:
            result.append(y)
    return min(result) if result else None

def main() -> None:
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument("--reference",type=Path,required=True)
    p.add_argument("--actual",type=Path,required=True)
    p.add_argument("--output",type=Path,required=True)
    p.add_argument("--max-drift-px",type=int,default=None,
                   help="Fail when any measured app-owned text glyph start differs by more than this many pixels")
    args=p.parse_args()
    lines=["# SYSTIVRA measured text-baseline alignment", "",
        "Compared only text pixels at fixed 390×844 locations; **not 1:1 approval**.",
        "Positive delta = Android glyph appears lower than the source SVG.", "",
        "| Screen | Text region | Reference top y | Android top y | Delta px |",
        "|---|---|---:|---:|---:|"]
    outliers = []
    for name in SCREENS:
        rp=args.reference/(name+".png")
        ap=args.actual/(name+".png")
        if not rp.is_file() or not ap.is_file():
            raise SystemExit("Missing same-name screenshot/reference: "+name)
        with Image.open(rp) as ref, Image.open(ap) as actual:
            if ref.size != (390,844) or actual.size != (390,844):
                raise SystemExit("Expected unscaled 390x844 native screenshots: "+name)
            for label,area,tone in REGIONS:
                expected=start_y(ref,area,tone)
                observed=start_y(actual,area,tone)
                if expected is None or observed is None:
                    raise SystemExit("Unable to locate "+label+" text pixels in "+name)
                delta = observed - expected
                lines.append(f"| {name} | {label} | {expected} | {observed} | {delta:+d} |")
                if args.max_drift_px is not None and abs(delta) > args.max_drift_px:
                    outliers.append(f"{name}: {label}: {delta:+d}px")
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text("\n".join(lines)+"\n",encoding="utf-8")
    print("\n".join(lines))
    if outliers:
        raise SystemExit("Text glyph baseline parity exceeded allowed drift: " + "; ".join(outliers))

if __name__=="__main__":
    main()
