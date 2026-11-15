#!/usr/bin/env bash
set -euo pipefail
# Manual only: requires a USB-authorized Android device physically connected
# to the operator's computer. This script is NOT run by GitHub Actions.
#
# Usage: bash tools/manual-physical-device-qa.sh <adb-serial> [debug.apk]
# It never uninstalls an existing app, resets data or modifies display settings.
if [[ $# -lt 1 ]]; then
  echo "Usage: bash $0 <adb-serial> [debug-apk]" >&2
  exit 2
fi
serial="$1"
apk="${2:-android/app/build/outputs/apk/debug/app-debug.apk}"
test -s "$apk" || { echo "Built debug APK not found at $apk" >&2; exit 1; }
command -v adb >/dev/null
adb -s "$serial" get-state | grep -Fx device
printf 'This will install/update the SYSTIVRA DEBUG build and open 8 app screens on your device.\n'
printf 'It will NOT reset the device or uninstall existing apps. Continue? [y/N] '
read -r consent
[[ "$consent" =~ ^[Yy]$ ]] || { echo "Cancelled without changes"; exit 2; }
output="${SYSTIVRA_QA_OUTPUT:-$(mktemp -d /tmp/systivra-physical-qa.XXXXXX)}"
mkdir -p "$output"
printf '%s\n' '# SYSTIVRA local physical-device QA' > "$output/report.md"
printf 'Android version: %s\n' "$(adb -s "$serial" shell getprop ro.build.version.release | tr -d '\r')" >> "$output/report.md"
printf 'Device model: %s\n' "$(adb -s "$serial" shell getprop ro.product.model | tr -d '\r')" >> "$output/report.md"
printf 'Real physical viewport: %s\n' "$(adb -s "$serial" shell wm size | tr -d '\r')" >> "$output/report.md"
printf 'Real device density: %s\n' "$(adb -s "$serial" shell wm density | tr -d '\r')" >> "$output/report.md"
printf 'Existing font scale: %s\n' "$(adb -s "$serial" shell settings get system font_scale | tr -d '\r')" >> "$output/report.md"
adb -s "$serial" install -r "$apk"
routes=(
  dashboard hardware-explorer benchmark-results app-usage
  session-analysis benchmark-running cpu-cores settings
)
for route in "${routes[@]}"; do
  adb -s "$serial" shell am force-stop com.brendigo.systivra || true
  adb -s "$serial" shell am start -W \
    -n com.brendigo.systivra/.MainActivity --es qa_route "$route" \
    > "$output/$route.launch.txt"
  sleep 2
  adb -s "$serial" shell pidof com.brendigo.systivra >/dev/null || {
    echo "FAIL: SYSTIVRA crashed during route $route" >&2
    exit 1
  }
  focused="$(adb -s "$serial" shell dumpsys window |
    grep -E 'mCurrentFocus=|mFocusedApp=' | head -n 2 || true)"
  [[ "$focused" == *"com.brendigo.systivra"* ]] || {
    echo "FAIL: SYSTIVRA is not focused at route $route: $focused" >&2
    exit 1
  }
  adb -s "$serial" exec-out screencap -p > "$output/$route.png"
  test -s "$output/$route.png"
  printf '\n- %s: real screen captured\n' "$route" >> "$output/report.md"
done
printf '\nAll eight foreground route captures succeeded. This does not certify 1:1.\n' >> "$output/report.md"
printf '\nInspect clipping, tap-target accessibility, OS insets, dark theme, rotation and TalkBack manually.\n' >> "$output/report.md"
echo "Physical-device QA evidence: $output"
