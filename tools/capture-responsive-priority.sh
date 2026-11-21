#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# Responsive capture matrix is diagnostic; it does not stretch 390x844 SVG art.
APP=com.brendigo.systivra
ACTIVITY="$APP/.MainActivity"
APK=android/app/build/outputs/apk/debug/app-debug.apk
OUT=qa/responsive
ROUTES=(
  dashboard
  hardware-explorer
  benchmark-results
  app-usage
  session-analysis
  benchmark-running
  cpu-cores
  settings
)
mkdir -p "$OUT/debug"
adb wait-for-device
adb install -r "$APK"
adb shell input keyevent 82 || true
adb shell settings put secure immersive_mode_confirmations confirmed || true
for dimensions in 360x800 412x915 600x960 800x1280; do
  W="${dimensions%x*}"
  H="${dimensions#*x}"
  dir="$OUT/$dimensions"
  mkdir -p "$dir"
  adb shell wm size "$dimensions"
  adb shell wm density 160
  # One explicit larger-font comparison; others preserve 100% reference font scale.
  if [[ "$dimensions" == "412x915" ]]; then
    adb shell settings put system font_scale 1.30
  else
    adb shell settings put system font_scale 1.0
  fi
  sleep 6
  adb shell wm size | grep -F "Override size: $dimensions"
  adb shell wm density | grep -F "Override density: 160"
  for route in "${ROUTES[@]}"; do
    echo "Capture $dimensions -> $route"
    adb shell am force-stop "$APP" || true
    adb shell am start -W -n "$ACTIVITY" --es qa_route "$route" \
      > "$OUT/debug/$dimensions-$route.launch.txt"
    sleep 1
    if ! adb shell pidof "$APP" >/dev/null; then
      adb logcat -d -t 600 > "$OUT/debug/$dimensions-$route.logcat.txt" || true
      echo "::error::App process missing: $dimensions $route"
      exit 1
    fi
    focus="$(adb shell dumpsys window | grep -E 'mCurrentFocus=|mFocusedApp=' | head -n 2 || true)"
    if [[ "$focus" != *"$APP"* ]]; then
      adb logcat -d -t 600 > "$OUT/debug/$dimensions-$route.logcat.txt" || true
      echo "::error::App lost focus: $dimensions $route: $focus"
      exit 1
    fi
    adb exec-out screencap -p > "$dir/$route.png"
  done
done
python3 tools/validate-responsive-priority.py --root "$OUT"
echo "All 4 viewport sizes and 8 real app routes captured; visual inspection still required."
