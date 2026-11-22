#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# Actual Android screenshots of UI STATES not represented by the 37 SVG boards.
# Never retouch. Keep separate from the strict 38 source-reference captures.
APP=com.brendigo.systivra
ACTIVITY="$APP/.MainActivity"
OUT=qa/reference-viewport/interactive
mkdir -p "$OUT"
adb wait-for-device
adb shell wm size | grep -F "Override size: 390x844" >/dev/null
adb shell wm density | grep -F "Override density: 160" >/dev/null

focus_and_capture() {
  local name="$1"
  if ! adb shell pidof "$APP" >/dev/null; then
    echo "::error::App process died during interaction capture: $name"
    return 1
  fi
  local window
  window="$(adb shell dumpsys window | grep 'mCurrentFocus=' | head -n 1 || true)"
  if [[ "$window" != *"$APP"* ]]; then
    echo "::error::Focused window is not SYSTIVRA on $name: $window"
    return 1
  fi
  adb exec-out screencap -p > "$OUT/$name.png"
  test -s "$OUT/$name.png"
}

for spec in 'appearance:392' 'update-interval:474' 'units:556'; do
  name="${spec%%:*}"
  y="${spec##*:}"
  adb shell am force-stop "$APP"
  adb shell am start -W -n "$ACTIVITY" --es qa_route settings >/dev/null
  sleep 2
  adb shell input tap 175 "$y"
  sleep 1
  focus_and_capture "settings-$name-dialog"
done

# Real disabled/empty data export controls, not illustrative mockups.
adb shell am force-stop "$APP"
adb shell am start -W -n "$ACTIVITY" --es qa_route reports-export-controls >/dev/null
sleep 2
focus_and_capture "reports-disabled-buttons"

python3 - <<'PY'
from pathlib import Path
from PIL import Image, ImageChops, ImageStat
root=Path("qa/reference-viewport")
output=root/"interactive"
paths=sorted(output.glob("*.png"))
if len(paths)!=4:
    raise SystemExit(f"Expected exactly 4 genuine interactive-state captures, got {len(paths)}")
base=Image.open(root/"screenshots/36-settings.png").convert("RGB")
for file in paths:
    with Image.open(file) as im:
        if im.size!=(390,844):
            raise SystemExit(f"Invalid actual viewport for {file}: {im.size}")
        if "dialog" in file.name:
            difference=ImageChops.difference(base,im.convert("RGB"))
            mae=sum(ImageStat.Stat(difference).mean)/3
            if mae < 4.0:
                raise SystemExit(f"Settings click did not visibly open dialog: {file.name}, MAE={mae:.2f}")
print("Verified four real app interaction screenshots, three actual settings dialogs.")
PY
