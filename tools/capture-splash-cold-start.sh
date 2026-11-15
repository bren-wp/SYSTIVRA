#!/usr/bin/env bash
set -euo pipefail
# Independent genuine Android system Splash capture; this is NOT an SVG render.
# Screenrecord starts before the app process so even short Splash frames are retained.
APP=com.brendigo.systivra
ACTIVITY="$APP/.MainActivity"
OUT=qa/splash
mkdir -p "$OUT/frames"
adb wait-for-device
adb shell wm size 390x844
adb shell wm density 160
adb shell settings put system font_scale 1.0
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb shell input keyevent KEYCODE_HOME
adb shell am force-stop "$APP" || true
adb shell rm -f /sdcard/systivra-splash.mp4 || true
adb logcat -c >/dev/null 2>&1 || true

adb shell screenrecord --time-limit 5 --bit-rate 4000000 /sdcard/systivra-splash.mp4 > "$OUT/screenrecord.log" 2>&1 &
RECORDER_PID=$!
sleep 1
adb shell am start -W -n "$ACTIVITY" > "$OUT/cold-launch.txt"
wait "$RECORDER_PID"
adb pull /sdcard/systivra-splash.mp4 "$OUT/genuine-splash-cold-start.mp4"
test -s "$OUT/genuine-splash-cold-start.mp4"
adb logcat -d -t 3000 > "$OUT/logcat.txt" 2>&1 || true
adb shell wm size > "$OUT/viewport.txt"
adb shell wm density >> "$OUT/viewport.txt"
grep -F 'Override size: 390x844' "$OUT/viewport.txt"
grep -F 'Override density: 160' "$OUT/viewport.txt"

# Extract temporal evidence of the launch, not a post-launch static screenshot.
ffmpeg -hide_banner -nostdin -loglevel error -y \
  -i "$OUT/genuine-splash-cold-start.mp4" \
  -vf fps=15 -frames:v 75 "$OUT/frames/cold-%03d.png"
python3 tools/validate-splash-capture.py \
  --frames "$OUT/frames" \
  --output "$OUT/frame-audit.json" \
  --contact-sheet "$OUT/cold-start-contact-sheet.png"
python3 - <<'PY'
import cairosvg
from pathlib import Path
out = Path("qa/splash/reference-original-390x844.png")
cairosvg.svg2png(url="brending/screens/01-splash.svg", write_to=str(out), output_width=390, output_height=844)
PY
echo "Archived the real pre-launch-to-Dashboard system Splash video and concept reference separately."
