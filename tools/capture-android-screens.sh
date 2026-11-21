#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# Capture only ACTUAL focused Android app screens; never save Quickstep or System UI ANRs.
APP=com.brendigo.systivra
ACTIVITY="$APP/.MainActivity"
APK=android/app/build/outputs/apk/debug/app-debug.apk
# Keep real Play-resolution screenshots and geometry-matched reference captures separate.
SCREENSHOT_DIR="${SCREENSHOT_DIR:-qa/screenshots}"
DEBUG_DIR="${DEBUG_DIR:-qa/debug}"
mkdir -p "$SCREENSHOT_DIR" "$DEBUG_DIR"
adb wait-for-device
adb install -r "$APK"
adb shell input keyevent 82 || true
# Suppress Android immersive-mode first-run tutorial that previously obscured screenshots.
adb shell settings put secure immersive_mode_confirmations confirmed || true
# Play-resolution workflow retains default wm geometry. The separate parity workflow explicitly sets reference geometry after boot with KVM.
adb shell wm size
adb shell wm density
adb shell settings put system font_scale 1.0
capture() {
  local basename="$1" route="$2" focus='' output="$SCREENSHOT_DIR/$1.png"
  echo "Capture $basename -> $route"
  adb shell am force-stop "$APP" || true
  adb shell am start -W -n "$ACTIVITY" --es qa_route "$route" > "$DEBUG_DIR/${basename}.launch.txt"
  sleep 2
  if ! adb shell pidof "$APP" >/dev/null; then
    echo "ERROR: app process absent ($route)"
    adb logcat -d -t 400 > "$DEBUG_DIR/${basename}.logcat.txt" || true
    return 1
  fi
  focus="$(adb shell dumpsys window | grep -E 'mCurrentFocus=|mFocusedApp=' | head -n 2 || true)"
  if [[ "$focus" != *"$APP"* ]]; then
    echo "ERROR: another window is focused ($route): $focus"
    adb logcat -d -t 400 > "$DEBUG_DIR/${basename}.logcat.txt" || true
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
validation_args=()
if [[ -n "${EXPECTED_CAPTURE_WIDTH:-}" && -n "${EXPECTED_CAPTURE_HEIGHT:-}" ]]; then
  validation_args+=(--width "$EXPECTED_CAPTURE_WIDTH" --height "$EXPECTED_CAPTURE_HEIGHT")
fi
python3 tools/validate-android-screens.py "$SCREENSHOT_DIR" "${validation_args[@]}"
echo "Captured $(find "$SCREENSHOT_DIR" -name '*.png' | wc -l) app screenshots; Splash requires separate startup capture."
