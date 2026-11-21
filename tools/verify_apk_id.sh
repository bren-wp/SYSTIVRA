#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
APK="${1:-android/app/build/outputs/apk/debug/app-debug.apk}"
test -s "$APK" || { echo "APK missing: $APK" >&2; exit 1; }
SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
AAPT="$(find "$SDK_ROOT/build-tools" -type f -name aapt | sort -V | tail -n 1)"
test -x "$AAPT" || { echo "Android aapt tool not installed" >&2; exit 1; }
INFO="$("$AAPT" dump badging "$APK")"
echo "$INFO" | head -n 1
echo "$INFO" | grep -Eq "^package: name='com\.brendigo\.systivra' " || { echo "Incorrect APK application ID; expected com.brendigo.systivra" >&2; exit 1; }
echo "Verified signed debug APK application ID: com.brendigo.systivra"
