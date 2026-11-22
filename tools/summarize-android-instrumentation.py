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


def observed_cases(directory: Path) -> tuple[set[str], list[str]]:
    """Read class coverage from JUnit XML, not from Gradle process exit status."""
    classes: set[str] = set()
    skipped: list[str] = []
    if not directory.is_dir():
        return classes, skipped
    for path in directory.rglob("*.xml"):
        try:
            root = ET.parse(path).getroot()
        except (ET.ParseError, OSError):
            continue
        suites = [root] if root.tag == "testsuite" else root.findall(".//testsuite")
        for suite in suites:
            for case in suite.findall("testcase"):
                cls = case.attrib.get("classname", suite.attrib.get("name", ""))
                if cls:
                    classes.add(cls)
                if case.find("skipped") is not None:
                    skipped.append(f"{cls}.{case.attrib.get('name', '?')}")
    return classes, skipped


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--results", type=Path, required=True)
    parser.add_argument("--summary", type=Path, required=True)
    parser.add_argument("--require-results", action="store_true",
                        help="Fail if the emulator did not produce parseable JUnit results")
    parser.add_argument("--require-classes", default="",
                        help="Comma-delimited fully qualified test classes expected in this shard")
    args = parser.parse_args()
    report = summarize(args.results)
    covered, skipped = observed_cases(args.results)
    required = {name.strip() for name in args.require_classes.split(",") if name.strip()}
    missing = sorted(required - covered)
    failures = []
    if args.require_results and "No parseable connected-test JUnit XML was produced." in report:
        failures.append("Connected Android tests produced no JUnit results")
    if missing:
        failures.append("Missing JUnit coverage for: " + ", ".join(missing))
    if required and skipped:
        failures.append("Mandatory test cases skipped: " + ", ".join(skipped))
    if required:
        report += f"\nRequired test classes: **{len(required)}**; reported: **{len(required - set(missing))}**.\n"
    if failures:
        report += "\n## INCOMPLETE INSTRUMENTATION\n" + "\n".join("- " + item for item in failures) + "\n"
    args.summary.parent.mkdir(parents=True, exist_ok=True)
    args.summary.write_text(report, encoding="utf-8")
    if failures:
        raise SystemExit("ERROR: " + "; ".join(failures))


if __name__ == "__main__":
    main()
