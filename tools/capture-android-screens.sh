#!/usr/bin/env bash
set -euo pipefail
# Capture only ACTUAL focused Android app screens; never save Quickstep or System UI ANRs.
APP=com.brendigo.systivra
ACTIVITY="$APP/.MainActivity"
APK=android/app/build/outputs/apk/debug/app-debug.apk
mkdir -p qa/screenshots qa/debug
adb wait-for-device
adb install -r "$APK"
adb shell input keyevent 82 || true
# Suppress Android immersive-mode first-run tutorial that previously obscured screenshots.
adb shell settings put secure immersive_mode_confirmations confirmed || true
# Do NOT set wm size/density: restarting Quickstep caused the older QA screenshots to show ANR dialogs.
adb shell wm size
adb shell wm density
adb shell settings put system font_scale 1.0
capture() {
  local basename="$1" route="$2" focus='' output="qa/screenshots/$1.png"
  echo "Capture $basename -> $route"
  adb shell am force-stop "$APP" || true
  adb shell am start -W -n "$ACTIVITY" --es qa_route "$route" > "qa/debug/${basename}.launch.txt"
  sleep 2
  if ! adb shell pidof "$APP" >/dev/null; then
    echo "ERROR: app process absent ($route)"
    adb logcat -d -t 400 > "qa/debug/${basename}.logcat.txt" || true
    return 1
  fi
  focus="$(adb shell dumpsys window | grep -E 'mCurrentFocus=|mFocusedApp=' | head -n 2 || true)"
  if [[ "$focus" != *"$APP"* ]]; then
    echo "ERROR: another window is focused ($route): $focus"
    adb logcat -d -t 400 > "qa/debug/${basename}.logcat.txt" || true
    return 1
  fi
  adb exec-out screencap -p > "$output"
  test -s "$output"
}
for ref in brending/screens/*.svg; do
  basename="$(basename "$ref" .svg)"
  route="${basename#??-}"
  [[ "$route" != splash ]] || continue
  capture "$basename" "$route"
done
capture 38-recording-library recording-library
capture 39-session-insights session-insights
python3 tools/validate-android-screens.py qa/screenshots
echo "Captured $(find qa/screenshots -name '*.png' | wc -l) app screenshots; Splash requires separate startup capture."
