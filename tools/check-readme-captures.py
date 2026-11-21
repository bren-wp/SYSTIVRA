#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Check README screenshot links against genuine captured-image *files*.

Structural validation does NOT prove app focus, freedom from system dialogs,
manual visual approval, or pixel-perfect parity. Those need emulator and human QA.
"""
from __future__ import annotations

import hashlib
from pathlib import Path
import re
import struct

ROOT = Path(__file__).resolve().parents[1]
README = ROOT / "README.md"
SCREENSHOTS = ROOT / "brending/google-play/real-screenshots"
EXPECTED = [
    "01-dashboard-1080x1920.png",
    "02-hardware-1080x1920.png",
    "03-memory-1080x1920.png",
    "04-battery-1080x1920.png",
    "05-monitor-1080x1920.png",
    "06-diagnostics-1080x1920.png",
    "07-recordings-1080x1920.png",
    "08-insights-1080x1920.png",
]
PNG_MAGIC = b"\x89PNG\r\n\x1a\n"


def main() -> None:
    readme = README.read_text(encoding="utf-8")
    title = "## 📱 Real Android application screenshots"
    if readme.count(title) != 1:
        raise SystemExit("README is missing its explicitly identified real-capture gallery")
    section = readme.split(title, 1)[1].split("## ", 1)[0]
    found = re.findall(r'brending/google-play/real-screenshots/([^"<>]+\.png)', section)
    if found != EXPECTED:
        raise SystemExit(f"README must reference all eight actual captures in order: {found!r}")
    if "pending" not in section.lower() and "not approved" not in section.lower():
        raise SystemExit("README must disclose the lack of manual Play screenshot approval")
    hashes: set[str] = set()
    for filename in EXPECTED:
        file = SCREENSHOTS / filename
        if not file.is_file():
            raise SystemExit(f"Capture missing: {file}")
        blob = file.read_bytes()
        if len(blob) < 512 or not blob.startswith(PNG_MAGIC):
            raise SystemExit(f"Invalid or empty PNG: {file}")
        if blob[12:16] != b"IHDR":
            raise SystemExit(f"Invalid PNG header: {file}")
        dimensions = struct.unpack(">II", blob[16:24])
        if dimensions != (1080, 1920):
            raise SystemExit(f"Wrong capture size: {file}: {dimensions}")
        digest = hashlib.sha256(blob).hexdigest()
        if digest in hashes:
            raise SystemExit(f"Duplicate screenshot bytes: {file}")
        hashes.add(digest)
    print("Verified eight present, nonidentical, 1080x1920 captured PNG files linked by README.")
    print("This is NOT Android-window-focus validation, manual approval or 1:1 certification.")


if __name__ == "__main__":
    main()
