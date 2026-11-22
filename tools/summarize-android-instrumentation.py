#!/usr/bin/env python3
# SYSTIVRA by Brendigo
"""Write concrete Android instrumented-test evidence to GitHub job summary.

Does not change or reinterpret the actual Gradle test exit code.
"""
import argparse
from pathlib import Path
import xml.etree.ElementTree as ET


def summarize(directory: Path) -> str:
    files = sorted(directory.rglob("*.xml")) if directory.is_dir() else []
    suites = []
    failed = []
    parsed = 0
    for path in files:
        try:
            root = ET.parse(path).getroot()
        except (ET.ParseError, OSError):
            continue
        elements = [root] if root.tag == "testsuite" else root.findall(".//testsuite")
        for suite in elements:
            tests = suite.findall("testcase")
            if not tests:
                continue
            parsed += 1
            suites.append((suite.attrib.get("name", path.stem), len(tests)))
            for case in tests:
                errors = case.findall("failure") + case.findall("error")
                for error in errors:
                    failed.append((
                        case.attrib.get("classname", suite.attrib.get("name", "?")),
                        case.attrib.get("name", "?"),
                        error.attrib.get("message", "") or (error.text or "")[:900]
                    ))
    lines = ["# SYSTIVRA Android connected UI test evidence", ""]
    if parsed == 0:
        lines += [
            "**No parseable connected-test JUnit XML was produced.**",
            "Inspect emulator logcat and job logs; absence of a report is not a test pass.",
            "",
        ]
    else:
        lines += [
            f"- JUnit suites: **{parsed}**",
            f"- Test cases reported: **{sum(count for _, count in suites)}**",
            f"- Test cases with failures/errors: **{len(failed)}**",
            "",
        ]
        for cls, method, description in failed[:30]:
            lines += [f"### FAIL: `{cls}.{method}`", "", "```text", description[:900], "```", ""]
        if not failed:
            lines += ["No failures recorded in parsed JUnit cases. Verify Gradle exit code and full device run.", ""]
    return "\n".join(lines) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--results", type=Path, required=True)
    parser.add_argument("--summary", type=Path, required=True)
    parser.add_argument("--require-results", action="store_true",
                        help="Fail if the emulator did not produce parseable JUnit results")
    args = parser.parse_args()
    report = summarize(args.results)
    args.summary.parent.mkdir(parents=True, exist_ok=True)
    args.summary.write_text(report, encoding="utf-8")
    if args.require_results and "No parseable connected-test JUnit XML was produced." in report:
        raise SystemExit("ERROR: Connected Android tests produced no JUnit results; not a pass.")


if __name__ == "__main__":
    main()
