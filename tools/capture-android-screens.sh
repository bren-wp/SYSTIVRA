#!/usr/bin/env bash
set -euo pipefail
# Engineering-only screenshots from a DEBUG Android APK.
# They are not evidence of pixel-perfect compliance without QA comparison.
APP=com.systivra.app
ACTIVITY="$APP/.MainActivity"
mkdir -p qa/screenshots
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell wm size 390x844
adb shell wm density 160
adb shell settings put system font_scale 1.0
for reference in brending/screens/*.svg; do
  basename="$(basename "$reference" .svg)"
  route="${basename#??-}"
  if [[ "$route" == splash ]]; then
    echo "Skipping system Splash: a delayed PNG would incorrectly show Dashboard"
    continue
  fi
  echo "Capturing $basename: $route"
  adb shell am force-stop "$APP" || true
  adb shell am start -n "$ACTIVITY" --es qa_route "$route" >/dev/null
  sleep 1.8
  adb exec-out screencap -p > "qa/screenshots/$basename.png"
  test -s "qa/screenshots/$basename.png"
done
for page in 38-recording-library 39-session-insights; do
  route="${page#??-}"
  adb shell am force-stop "$APP" || true
  adb shell am start -n "$ACTIVITY" --es qa_route "$route" >/dev/null
  sleep 1.8
  adb exec-out screencap -p > "qa/screenshots/$page.png"
  test -s "qa/screenshots/$page.png"
done
echo "Captured $(find qa/screenshots -name '*.png' | wc -l) app screens; extras have no approved baseline. System Splash requires separate verification."
