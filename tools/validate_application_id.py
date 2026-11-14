#!/usr/bin/env python3
"""Hard fail when Android application identity deviates from BRENDIGO's ID.

This guards accidental source/namespace/package mismatch and prevents signing
or releasing an APK under any previous application ID.
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "android"
APP = ANDROID / "app"
TARGET = "com.brendigo.systivra"
REL = Path(*TARGET.split("."))

gradle = (APP / "build.gradle.kts").read_text(encoding="utf-8")
def require(pattern: str, location: str) -> None:
    if not re.search(pattern, gradle):
        raise SystemExit(f"INVALID APPLICATION ID: {location} must be {TARGET}")

require(r'namespace\s*=\s*"com\.brendigo\.systivra"', "namespace")
require(r'applicationId\s*=\s*"com\.brendigo\.systivra"', "applicationId")

sources = [
    APP / "src/main/java",
    APP / "src/test/java",
    APP / "src/androidTest/java",
]
for directory in sources:
    desired = directory / REL
    if not desired.is_dir():
        raise SystemExit(f"INVALID SOURCE PATH: {desired.relative_to(ROOT)}")
    files = sorted(directory.rglob("*.kt"))
    if not files:
        raise SystemExit(f"NO KOTLIN SOURCES: {directory}")
    for source in files:
        rel = source.relative_to(directory)
        if rel.parts[:3] != REL.parts:
            raise SystemExit(f"WRONG KOTLIN PATH: {source.relative_to(ROOT)}")
        contents = source.read_text(encoding="utf-8")
        actual_package = re.search(r"(?m)^package\s+(\S+)", contents)
        if not actual_package or not (
            actual_package.group(1) == TARGET
            or actual_package.group(1).startswith(TARGET + ".")
        ):
            raise SystemExit(f"WRONG KOTLIN PACKAGE: {source.relative_to(ROOT)}")
        if "com.systivra.app" in contents:
            raise SystemExit(f"OLD APPLICATION REFERENCE: {source.relative_to(ROOT)}")

capture = (ROOT / "tools/capture-android-screens.sh").read_text(encoding="utf-8")
if 'APP=com.brendigo.systivra' not in capture:
    raise SystemExit("Android screenshot capture uses the wrong application ID")
manifest = (APP / "src/main/AndroidManifest.xml").read_text(encoding="utf-8")
if 'android:name=".MainActivity"' not in manifest:
    raise SystemExit("Launcher activity must resolve relative to com.brendigo.systivra")
if re.search(r'<uses-permission[^>]*android\.permission\.INTERNET', manifest):
    raise SystemExit("INTERNET permission was added; complete a new privacy review")
print(f"PASS: namespace, applicationId, all Kotlin paths, tests, Android manifest and QA target: {TARGET}")
