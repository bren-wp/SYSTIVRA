#!/usr/bin/env python3
"""Render exact Google Play graphics from versioned SVG sources.

Only icon and feature graphic are upload-ready illustrations.
Marketing concept posters are NOT actual Android screenshots.
"""
from pathlib import Path
import io
import json
import zipfile

from PIL import Image
import cairosvg

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "brending/google-play/source"
OUT = ROOT / "brending/google-play/assets"
PREVIEW = ROOT / "brending/google-play/concepts-NOT-FOR-PLAY"
OUT.mkdir(parents=True, exist_ok=True)
PREVIEW.mkdir(parents=True, exist_ok=True)

def render(src: Path, output: Path, size: tuple[int, int], mode: str) -> None:
    png = cairosvg.svg2png(url=str(src), output_width=size[0], output_height=size[1])
    im = Image.open(io.BytesIO(png)).convert(mode)
    if im.size != size:
        raise ValueError(f"{src.name}: invalid dimensions {im.size}")
    im.save(output, format="PNG", optimize=True)
    assert Image.open(output).mode == mode

render(SOURCE / "play-icon.svg", OUT / "icon-512.png", (512, 512), "RGBA")
render(SOURCE / "feature-graphic.svg", OUT / "feature-graphic-1024x500.png", (1024, 500), "RGB")
if (OUT / "icon-512.png").stat().st_size > 1_048_576:
    raise RuntimeError("Store icon must be at most 1 MB")

for file in sorted((SOURCE / "previews").glob("*.svg")):
    render(file, PREVIEW / f"{file.stem}-1080x1920.png", (1080, 1920), "RGB")

meta = {
    "app_name": "SYSTIVRA",
    "publisher": "BRENDIGO",
    "assets_ready_for_store": {
        "icon": "assets/icon-512.png",
        "feature_graphic": "assets/feature-graphic-1024x500.png"
    },
    "screenshots_ready": False,
    "reason": "Concept SVG/PNG files are not real captures of the running Android app. "
              "Google Play requires authentic screenshots; use stable emulator/phone captures."
}
(OUT / "manifest.json").write_text(json.dumps(meta, indent=2), encoding="utf-8")

archive = OUT / "SYSTIVRA-Play-Graphics.zip"
with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as z:
    for path in sorted(OUT.glob("*.png")):
        z.write(path, path.name)
    for path in sorted(PREVIEW.glob("*.png")):
        z.write(path, "concepts-NOT-FOR-PLAY/" + path.name)
    z.write(OUT / "manifest.json", "manifest.json")
print("Created", archive, "icon bytes", (OUT / "icon-512.png").stat().st_size)
