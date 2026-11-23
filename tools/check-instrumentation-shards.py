#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Fail CI if an Android instrumentation test class is missing from the matrix.

The class inventory comes from source, not from a manually maintained list.
Keep all UI assertions enabled: sharding changes scheduling only.
"""
from collections import Counter
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
TESTS = ROOT / "android/app/src/androidTest"
WORKFLOW = ROOT / ".github/workflows/android-reference-viewport.yml"


def discover_tests() -> set[str]:
    found = set()
    for file in sorted(TESTS.rglob("*.kt")):
        source = file.read_text(encoding="utf-8")
        if "@Test" not in source:
            continue
        # The project's instrumented screens use Column.verticalScroll.
        # performScrollToNode searches LazyList semantics and has previously
        # hung two shards for 22 minutes with zero JUnit XML. Its replacement
        # is target.performScrollTo() plus assertIsDisplayed(), which still
        # checks the real scroll behaviour instead of hiding it.
        if re.search(r"\.performScrollToNode\s*\(", source):
            raise ValueError(
                f"{file}: performScrollToNode is unsafe for these Column.verticalScroll "
                "tests; scroll the tagged target via performScrollTo instead"
            )
        package = re.search(r"(?m)^package\s+([\w.]+)\s*$", source)
        klass = re.search(r"(?m)^\s*(?:public\s+|internal\s+)?class\s+(\w+)\b", source)
        if package is None or klass is None:
            raise ValueError(f"Cannot identify test class in {file}")
        found.add(package.group(1) + "." + klass.group(1))
    if not found:
        raise ValueError("No Android instrumentation test source files found")
    return found


def discover_shards() -> list[list[str]]:
    workflow = WORKFLOW.read_text(encoding="utf-8")
    matches = re.findall(r'(?m)^\s+classes:\s*"([^"]+)"\s*$', workflow)
    if not matches:
        raise ValueError("No instrumented test matrix classes found")
    shards = [[name.strip() for name in match.split(",")] for match in matches]
    if any(not shard or any(not name for name in shard) for shard in shards):
        raise ValueError("Instrumented test matrix contains an empty class")
    return shards


def validate() -> str:
    expected = discover_tests()
    shards = discover_shards()
    counts = Counter(test for shard in shards for test in shard)
    missing = expected - counts.keys()
    unknown = counts.keys() - expected
    duplicated = {name for name, count in counts.items() if count != 1}
    if missing or unknown or duplicated:
        raise ValueError(
            f"Missing: {sorted(missing)}; unknown: {sorted(unknown)}; "
            f"duplicated: {sorted(duplicated)}"
        )
    return f"Validated {len(expected)} Android instrumentation classes across {len(shards)} isolated shards."


if __name__ == "__main__":
    try:
        print(validate())
    except (ValueError, OSError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(1)
