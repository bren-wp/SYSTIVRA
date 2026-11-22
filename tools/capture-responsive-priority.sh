#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# Every non-Splash reference route plus the two additional functional screens
# is captured from the INSTALLED APK, not from resized concept renders.
APP=com.brendigo.systivra
ACTIVITY="$APP/.MainActivity"
APK=android/app/build/outputs/apk/debug/app-debug.apk
dimensions="${SYSTIVRA_VIEWPORT:?SYSTIVRA_VIEWPORT must select one matrix viewport}"
case "$dimensions" in
  360x800|412x915|600x960|800x1280|960x600) ;;
  *) echo "::error::Unsupported viewport: $dimensions"; exit 2 ;;
esac
OUT="qa/responsive/$dimensions"
mkdir -p "$OUT/debug"
adb wait-for-device
adb install -r "$APK"
adb shell input keyevent 82 || true
adb shell settings put secure immersive_mode_confirmations confirmed || true
adb shell wm size "$dimensions"
adb shell wm density 160
if [[ "$dimensions" == 412x915 ]]; then
  adb shell settings put system font_scale 1.30
else
  adb shell settings put system font_scale 1.0
fi
sleep 7
adb shell wm size | grep -F "Override size: $dimensions"
adb shell wm density | grep -F "Override density: 160"

capture() {
  local route="$1" name="$2" focus=''
  echo "Responsive genuine capture: $dimensions / $route"
  adb shell am force-stop "$APP" || true
  adb shell am start -W -n "$ACTIVITY" --es qa_route "$route"       > "$OUT/debug/$name.launch.txt"
  sleep 1
  if ! adb shell pidof "$APP" >/dev/null; then
    adb logcat -d -t 600 > "$OUT/debug/$name.logcat.txt" || true
    echo "::error::App process absent for $dimensions: $route"
    return 1
  fi
  focus="$(adb shell dumpsys window | grep -E 'mCurrentFocus=|mFocusedApp=' | head -n 2 || true)"
  if [[ "$focus" != *"$APP"* ]]; then
    adb logcat -d -t 600 > "$OUT/debug/$name.logcat.txt" || true
    echo "::error::App lost focus for $dimensions: $route ($focus)"
    return 1
  fi
  adb exec-out screencap -p > "$OUT/$name.png"
  test -s "$OUT/$name.png"
}

for ref in brending/screens/*.svg; do
  name="$(basename "$ref" .svg)"
  route="${name#??-}"
  [[ "$route" == splash ]] && continue
  capture "$route" "$name"
done
capture recording-library 38-recording-library
capture session-insights 39-session-insights

python3 tools/validate-responsive-priority.py --root qa/responsive --viewport "$dimensions"
echo "Validated 38 genuine Android app screens at $dimensions. Splash is validated separately."
