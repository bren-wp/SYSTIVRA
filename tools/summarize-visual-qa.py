#!/usr/bin/env python3
"""Generate an evidence-based Android screen QA report without claiming 1:1 parity.

Each original in-app reference must have a valid screenshot comparison.
A measured difference is evidence of a mismatch, not visual approval.
"""
import argparse
import csv
import math
from pathlib import Path
from statistics import mean

ORIGINAL_ROUTES = {f"{number:02d}" for number in range(2, 38)}

def load_report(path: Path):
    with path.open(newline="", encoding="utf-8") as source:
        records = list(csv.DictReader(source))
    route_ids = [record["screen"][:2] for record in records]
    if len(records) != 36 or set(route_ids) != ORIGINAL_ROUTES:
        raise ValueError("Expected all 36 unique original in-app routes (02–37)")
    results = []
    for record in records:
        if record.get("status") != "Compared":
            raise ValueError(f"Missing/invalid app comparison for {record['screen']}: {record.get('status')}")
        mae = float(record["mean_abs_error_percent"])
        changed = float(record["changed_pixels_percent"])
        if not all(math.isfinite(value) and 0 <= value <= 100 for value in (mae, changed)):
            raise ValueError(f"Invalid comparison values for {record['screen']}")
        results.append((record["screen"], mae, changed))
    return results


def summarize(results):
    by_changed = sorted(results, key=lambda row: row[2], reverse=True)
    lines = [
        "# SYSTIVRA Android visual QA — actual screenshot comparisons",
        "",
        "**Not pixel-perfect verified. 0/37 approved reference designs.**",
        "",
        f"- Valid in-app screenshot/reference comparisons: **{len(results)}/36**",
        "- Android system Splash: **separate startup capture required**",
        "- Recording Library and Session Insights: **no approved reference baseline**",
        f"- Mean absolute RGB error: **{mean(row[1] for row in results):.3f}%**",
        f"- Average changed-pixel percentage: **{mean(row[2] for row in results):.3f}%**",
        "",
        "These are raw image differences, not a visual similarity score. "
        "The reference boards use a 390×844 layout inside a 1080×1920 letterboxed PNG, "
        "while the emulator renders the actual responsive app at 1080×1920. "
        "Insets, density, font rasterization and dynamic hardware data differ. "
        "Do not mark a screen VERIFIED 1:1 until a same-viewport approved baseline "
        "and interactive/manual QA have passed.",
        "",
        "## Largest differences",
        "",
        "| Screen | Mean RGB error | Changed pixels |",
        "|---|---:|---:|",
    ]
    for name, mae, changed in by_changed[:8]:
        lines.append(f"| {name} | {mae:.3f}% | {changed:.3f}% |")
    lines += [
        "",
        "## All original in-app comparisons",
        "",
        "| Screen | Mean RGB error | Changed pixels |",
        "|---|---:|---:|",
    ]
    for name, mae, changed in sorted(results):
        lines.append(f"| {name} | {mae:.3f}% | {changed:.3f}% |")
    lines.append("")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        report = summarize(load_report(args.report))
    except (OSError, ValueError, KeyError) as exc:
        parser.exit(1, f"FAIL: cannot validate visual QA evidence: {exc}\n")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(report, encoding="utf-8")
    print(f"Visual evidence summarized: {args.output}; 0/37 verified 1:1.")


if __name__ == "__main__":
    main()
