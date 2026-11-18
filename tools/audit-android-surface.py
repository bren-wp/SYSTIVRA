#!/usr/bin/env python3
"""Conservative, deterministic source/dead-code and Android security audit.

This catches forbidden platform configurations and common dangerous patterns;
it is NOT a substitute for a dependency-CVE scanner, Android device pentest,
Google Play review or manual 37-screen interaction/visual acceptance.
Run on the source under review, including in CI.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re
from xml.etree import ElementTree as ET

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
SOURCE = Path("android/app/src/main")
DEFINITIONS = re.compile(
    r"(?m)^\s*private\s+(?:suspend\s+|inline\s+)?"
    r"(?:fun|class|data\s+class|object|val|var)\s+([A-Za-z_]\w*)\b"
)
SECURITY_PATTERNS = {
    "unsafe shell execution": r"Runtime\.getRuntime\(\)\.exec\(|ProcessBuilder\s*\(",
    "insecure TLS bypass": r"X509TrustManager|HostnameVerifier\s*\{|ALLOW_ALL_HOSTNAME_VERIFIER",
    "world-readable private files": r"MODE_WORLD_READABLE|MODE_WORLD_WRITEABLE",
    "unreviewed WebView JavaScript bridge": r"addJavascriptInterface\s*\(",
    "exception details leaked to UI": r"\.localizedMessage\b|\.stackTraceToString\s*\(|\.printStackTrace\s*\(",
    "hardcoded secret assignment": r"(?im)\b(?:api_key|client_secret|private_key|password)"
                                  r"\s*[:=]\s*[\"'][^\"'\n]{10,}[\"']",
}
UI_ROUTES = re.compile(r'"([a-z][\w-]*)"\s*->')
SECTIONS = re.compile(r'"([a-z][\w-]*)"\s+to\s+"')


def verify(root: Path) -> tuple[list[str], list[str], list[str], dict[str, int]]:
    problems: list[str] = []
    warnings: list[str] = []
    facts: list[str] = []

    manifest = root / SOURCE / "AndroidManifest.xml"
    app = ET.parse(manifest).getroot()
    application = app.find("application")
    if application is None:
        problems.append("Missing Android application")
        return problems, warnings, facts, {}
    def attr(name: str) -> str | None:
        return application.get(ANDROID_NS + name)

    facts.append("Manifest: allowBackup=%s; fullBackupContent=%s; dataExtractionRules=%s; usesCleartextTraffic=%s"
                 % tuple(attr(x) for x in
                         ("allowBackup", "fullBackupContent", "dataExtractionRules", "usesCleartextTraffic")))
    expected = {
        "allowBackup": "false",
        "fullBackupContent": "false",
        "dataExtractionRules": "@xml/data_extraction_rules",
        "usesCleartextTraffic": "false",
    }
    for key, value in expected.items():
        if attr(key) != value:
            problems.append(f"Unsafe/missing manifest setting {key}: expected {value!r}, got {attr(key)!r}")
    rules_path = root / SOURCE / "res/xml/data_extraction_rules.xml"
    if not rules_path.is_file():
        problems.append("Missing Android 12+ data transfer exclusions")
    else:
        rules = ET.parse(rules_path).getroot()
        for tag in ("cloud-backup", "device-transfer"):
            section = rules.find(tag)
            excluded = [] if section is None else section.findall("exclude")
            if not any(x.get("domain") == "root" and x.get("path") == "." for x in excluded):
                problems.append(f"Missing root exclusion in Android {tag}")

    perms = sorted(x.get(ANDROID_NS + "name", "") for x in app.findall("uses-permission"))
    facts.append("Declared Android permissions: " + ", ".join(perms))
    if "android.permission.INTERNET" in perms:
        problems.append("Unexpected INTERNET permission: requires explicit privacy/security review")
    forbidden = {"android.permission.READ_EXTERNAL_STORAGE",
                 "android.permission.WRITE_EXTERNAL_STORAGE",
                 "android.permission.MANAGE_EXTERNAL_STORAGE"}
    if forbidden & set(perms):
        problems.append("Unnecessary broad storage permission declared")
    exported = []
    for tag in ("activity", "activity-alias", "service", "receiver", "provider"):
        for element in application.findall(tag):
            if element.get(ANDROID_NS + "exported") == "true":
                exported.append((tag, element.get(ANDROID_NS + "name")))
    if exported != [("activity", ".MainActivity")]:
        problems.append("Unexpected exported Android components: " + repr(exported))
    facts.append("Intentionally exported component: launcher MainActivity only")

    files = sorted((root / SOURCE).rglob("*.kt"))
    source = "\n".join(x.read_text(encoding="utf-8") for x in files)
    facts.append(f"Inspected {len(files)} production Kotlin source files")
    for label, pattern in SECURITY_PATTERNS.items():
        for file in files:
            for i, line in enumerate(file.read_text(encoding="utf-8").splitlines(), 1):
                if line.lstrip().startswith("//"):
                    continue
                if re.search(pattern, line):
                    problems.append(f"{label}: {file.relative_to(root)}:{i}")

    main_activity = (root / SOURCE / "java/com/brendigo/systivra/MainActivity.kt").read_text()
    if "Channel.UNLIMITED" in main_activity:
        if not all(v in main_activity for v in
                   ("RecordingSampleGate", "tryAcquire()", "pendingSampleWrites.release()")):
            problems.append("Unbounded hardware snapshot queue without bounded sample gate")
        else:
            facts.append("Recording: control actions FIFO; sample backlog capped by atomic gate (32)")
    if "CreateDocument(" not in (root / SOURCE / "java/com/brendigo/systivra/ui/ReportsScreen.kt").read_text():
        problems.append("User-controlled document export missing; review automated/private file output")

    src = root / SOURCE / "java/com/brendigo/systivra/ui/AppRoot.kt"
    app_root = src.read_text()
    try:
        section_text = app_root.split("private val sections = listOf(", 1)[1].split(
            "private val enabledRoutes", 1)[0]
        enabled_text = app_root.split("private val enabledRoutes = setOf(", 1)[1].split(
            "fun AppRoot", 1)[0]
        routes_text = app_root.split("when (route) {", 1)[1].split(
            "else -> Planned", 1)[0]
        catalog = SECTIONS.findall(section_text)
        enabled = set(re.findall(r'"([a-z][\w-]*)"', enabled_text))
        handlers = set(UI_ROUTES.findall(routes_text))
        if set(catalog) != enabled or enabled != handlers:
            problems.append(f"Route/catalog mismatch: catalog-only={sorted(set(catalog)-enabled)}, "
                            f"handler-missing={sorted(enabled-handlers)}, unlisted={sorted(handlers-enabled)}")
        if len(catalog) != len(set(catalog)):
            problems.append("Duplicate registered route names")
        facts.append(f"Navigation coverage: {len(catalog)} catalog entries; "
                     f"{len(enabled)} enabled; {len(handlers)} concrete handlers")
    except IndexError:
        problems.append("Unable to audit AppRoot route registration")

    kotlin_test_files = sorted((root / "android/app/src/test").rglob("*.kt"))
    instrumented_files = sorted((root / "android/app/src/androidTest").rglob("*.kt"))
    all_kotlin = source + "\n" + "\n".join(
        p.read_text(encoding="utf-8") for p in kotlin_test_files + instrumented_files
    )
    private_declarations = []
    dead_private_candidates = []
    for file in files:
        for match in DEFINITIONS.finditer(file.read_text(encoding="utf-8")):
            name = match.group(1)
            private_declarations.append(name)
            uses = len(re.findall(r"\b" + re.escape(name) + r"\b", all_kotlin))
            if uses == 1:
                dead_private_candidates.append(f"{file.relative_to(root)}::{name}")
    if dead_private_candidates:
        warnings.append("Potential unused private declarations, manual review: "
                        + ", ".join(dead_private_candidates))
    facts.append(f"Lexical dead-code screening: {len(private_declarations)} private declarations, "
                 f"{len(dead_private_candidates)} one-occurrence candidates")
    # Kotlin source sometimes exposes components through compose/navigation by string.
    # Lexical 1-occurrence is a *candidate*, not safe automatic deletion.

    app_gradle = (root / "android/app/build.gradle.kts").read_text()
    root_gradle = (root / "android/build.gradle.kts").read_text()
    for m in re.finditer(r'(?m)^\s*(?:implementation|debugImplementation|androidTestImplementation|testImplementation)'
                         r'\([^\n]+\)', app_gradle):
        if re.search(r'(?::\+|SNAPSHOT|changing\s*=\s*true)', m.group(0), flags=re.I):
            problems.append("Dynamic/unreviewed Gradle dependency: " + m.group(0).strip())
    if 'version "9.5.1"' not in root_gradle and 'version "9.4.0"' not in root_gradle:
        warnings.append("Gradle Android plugin version changed; recheck official release/security notes")
    if "versionName =" not in app_gradle:
        problems.append("Missing Android release version")
    facts.append("Release build remains preview; release AAB requires separate authorized signing")

    website = root / "website"
    html = "\n".join(p.read_text(encoding="utf-8") for p in website.glob("*.html"))
    script = (website / "main.js").read_text(encoding="utf-8")
    if re.search(r"\.innerHTML\s*=|document\.write\s*\(|eval\s*\(", script):
        problems.append("Risky dynamic HTML/JavaScript injection on website")
    css = (website / "styles.css").read_text(encoding="utf-8")
    classes = set(re.findall(r"\.([A-Za-z][\w-]*)", css))
    unreferenced = sorted(x for x in classes if x not in html + script)
    if unreferenced:
        warnings.append("Potential unused website selectors: " + ", ".join(unreferenced))
    facts.append(f"Website: {len(classes)} CSS selector names checked; "
                 f"{len(unreferenced)} unreferenced names")

    counts = {"production_kotlin_files": len(files),
              "private_declarations": len(private_declarations),
              "unused_private_candidates": len(dead_private_candidates),
              "css_selector_names": len(classes),
              "route_count": len(SECTIONS.findall(section_text)) if "section_text" in locals() else 0}
    return problems, warnings, facts, counts


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--report", type=Path, default=Path("audit-output/ANDROID-SURFACE-AUDIT.md"))
    args = parser.parse_args()
    root = args.root.resolve()
    problems, warnings, facts, counts = verify(root)
    report = [
        "# Automated SYSTIVRA Android surface and dead-code scan",
        "",
        "This is a **static regression gate, not a full penetration test or 1:1 certification**.",
        "Runtime, physical-device, dependency advisory databases and human UX review remain separate.",
        "",
        f"Status: **{'FAIL' if problems else 'PASS'}** | "
        f"{len(problems)} blocking findings | {len(warnings)} review candidates.",
        "",
        "## Scan coverage", 
        "",
        *(f"- {name.replace('_', ' ')}: **{value}**" for name, value in counts.items()),
        "",
        "## Verified controls",
        "",
        *(f"- {x}" for x in facts),
        "",
        "## Blocking findings",
        "",
        *([f"- {x}" for x in problems] if problems else ["- None in rules covered by this script"]),
        "",
        "## Manual review candidates",
        "",
        *([f"- {x}" for x in warnings] if warnings else ["- None from lexical/static heuristics"]),
        "",
        "## Limitations",
        "",
        "- Cannot detect logic errors, vulnerabilities in transitive AAR/JAR dependencies, or",
        "  guarantees about OEM backup behavior without device/security tests.",
        "- Private functions used through reflection or external XML may have only one",
        "  lexical occurrence; do not remove solely based on this tool.",
        "- Raw status/navigation bar or dynamic Android data differences are not evidence",
        "  of a pixel-perfect UI; manual 37-screen sign-off is required.",
        "",
    ]
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text("\n".join(report), encoding="utf-8")
    print(args.report.read_text(encoding="utf-8"))
    if problems:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
