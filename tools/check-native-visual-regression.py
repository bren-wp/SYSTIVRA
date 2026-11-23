#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Reject large same-viewport screenshot regressions against real main-branch captures.

This is NOT a pixel-perfect acceptance certificate. Android status bars, dynamic
device readings and rasterization vary; numeric tolerances accommodate that
noise while catching substantial per-screen deterioration.
"""
import argparse
import csv
import math
from pathlib import Path
import sys


def load(path: Path) -> dict[str, tuple[float, float]]:
    result = {}
    with path.open(newline="", encoding="utf-8") as stream:
        reader = csv.DictReader(stream)
        required = {"screen", "mean_abs_error_percent", "changed_pixels_percent", "status"}
        if not reader.fieldnames or not required.issubset(reader.fieldnames):
            raise ValueError(f"{path}: missing visual report columns")
        for row in reader:
            name = row["screen"]
            if not name or name in result or row["status"] != "Compared":
                raise ValueError(f"{path}: duplicate, missing or incomplete result for {name!r}")
            values = (float(row["mean_abs_error_percent"]), float(row["changed_pixels_percent"]))
            if not all(math.isfinite(value) and 0 <= value <= 100 for value in values):
                raise ValueError(f"{path}: invalid visual percentage for {name!r}")
            result[name] = values
    if not result:
        raise ValueError(f"{path}: no comparable screenshots")
    return result


def compare(reference: dict, actual: dict, mae_growth: float, changed_growth: float) -> list[str]:
    if reference.keys() != actual.keys():
        missing = sorted(reference.keys() - actual.keys())
        extra = sorted(actual.keys() - reference.keys())
        raise ValueError(f"screen inventory mismatch: missing={missing}, extra={extra}")
    errors = []
    for screen in sorted(reference):
        mae, changed = actual[screen]
        base_mae, base_changed = reference[screen]
        if mae > base_mae + mae_growth or changed > base_changed + changed_growth:
            errors.append(
                f"{screen}: MAE {mae:.3f}% (baseline {base_mae:.3f}%), "
                f"changed {changed:.3f}% (baseline {base_changed:.3f}%)"
            )
    return errors


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--baseline", type=Path, required=True)
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument("--max-mae-growth", type=float, default=0.8)
    parser.add_argument("--max-changed-growth", type=float, default=5.0)
    args = parser.parse_args()
    if not all(math.isfinite(x) and x >= 0 for x in
               (args.max_mae_growth, args.max_changed_growth)):
        parser.error("growth tolerances must be finite non-negative percentages")
    try:
        baseline = load(args.baseline)
        current = load(args.report)
        if len(baseline) != 36:
            raise ValueError(f"expected 36 reference screens, got {len(baseline)}")
        failures = compare(baseline, current, args.max_mae_growth, args.max_changed_growth)
    except (OSError, ValueError) as error:
        print(f"ERROR: visual regression verification failed: {error}", file=sys.stderr)
        return 2
    print(f"SYSTIVRA: {len(current)}/36 real 390x844 comparisons checked against main baseline.")
    print("No screen is certified pixel-perfect by this regression check.")
    for failure in failures:
        print("REGRESSION: " + failure, file=sys.stderr)
    if failures:
        print(f"ERROR: {len(failures)} screens exceeded their baseline tolerance.", file=sys.stderr)
        return 1
    print("PASS: no excessive per-screen screenshot regression relative to baseline.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
