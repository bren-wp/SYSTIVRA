#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# This script is deliberately invoked through bash: emulator-runner executes
# its script field using /bin/sh, which does NOT support 'set -o pipefail'.
# Keep the full app screen, including unmodified Android system bars, as evidence.
adb wait-for-device
adb shell wm size 390x844
adb shell wm density 160
adb shell settings put system font_scale 1.0
sleep 8
adb shell wm size | grep -F "Override size: 390x844"
adb shell wm density | grep -F "Override density: 160"
SCREENSHOT_DIR=qa/reference-viewport/screenshots \
DEBUG_DIR=qa/reference-viewport/debug \
EXPECTED_CAPTURE_WIDTH=390 EXPECTED_CAPTURE_HEIGHT=844 \
  bash tools/capture-android-screens.sh
