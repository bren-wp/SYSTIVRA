#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Fail if an approved English SVG label lacks a Croatian UI translation.

The source boards are kept intact; the English baseline stays identical.
This checks the actual 37 SVG text nodes, not a hand-picked sample list.
"""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
BOARDS = ROOT / "brending" / "screens"
UI = ROOT / "android" / "app" / "src" / "main" / "java" / "com" / "brendigo" / "systivra" / "ui"
SOURCE = UI / "Localization.kt"
SKIP = {"10:24", "◆ ▴ ▰", "◇", "▤", "◉", "⌁", "›"}
material_import = "import androidx.compose.material3.Text"


def main() -> int:
    source = SOURCE.read_text(encoding="utf-8")
    catalog = re.search(
        r'private val croatianUi = """(.*?)"""\.trimIndent\(\)',
        source, flags=re.DOTALL,
    )
    if not catalog:
        raise SystemExit("FAIL: Croatian translation catalog is missing")
    keys = set()
    for line in catalog.group(1).splitlines():
        if not line.strip():
            continue
        if "|" not in line:
            raise SystemExit(f"FAIL: malformed dictionary row: {line!r}")
        key, translated = line.split("|", 1)
        if not key or not translated:
            raise SystemExit(f"FAIL: empty translation: {line!r}")
        keys.add(key)

    refs = sorted(BOARDS.glob("*.svg"))
    if len(refs) != 37 or {r.name[:2] for r in refs} != {
        f"{index:02}" for index in range(1, 38)
    }:
        raise SystemExit(f"FAIL: expected all 37 original SVG boards; got {len(refs)}")
    missing = set()
    labels = 0
    for ref in refs:
        svg = ET.fromstring(ref.read_text(encoding="utf-8"))
        for node in svg.iter():
            if node.tag.rsplit("}", 1)[-1] != "text":
                continue
            text = "".join(node.itertext()).strip()
            if not text or text in SKIP:
                continue
            labels += 1
            if text not in keys:
                missing.add(f"{ref.name}: {text}")

    unlocalized = []
    for path in UI.glob("*.kt"):
        if path.name == "Localization.kt":
            continue
        contents = path.read_text(encoding="utf-8")
        if re.search(r"^" + re.escape(material_import) + r"$", contents, flags=re.MULTILINE):
            unlocalized.append(path.name)

    errors = []
    if missing:
        errors.append("Missing Croatian text entries in SVG references: " + "; ".join(sorted(missing)))
    if unlocalized:
        errors.append("UI files bypassing the localized Text adapter: " + ", ".join(sorted(unlocalized)))
    print(f"Checked {labels} source text labels on {len(refs)} SVG boards; {len(keys)} Croatian keys.")
    for error in errors:
        print("FAIL: " + error, file=sys.stderr)
    if errors:
        return 1
    print("PASS: every SVG label has Croatian copy and all Compose UI texts use the language adapter.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
