#!/usr/bin/env bash
set -uo pipefail
# Run device UI tests on a fresh KVM emulator, not on the emulator used to
# force-stop 38 activities and capture 38 screenshots.
# Preserve the original Gradle exit code AND collect debug evidence on failure.
mkdir -p qa/instrumentation
adb logcat -c >/dev/null 2>&1 || true
status=0
gradle -p android :app:connectedDebugAndroidTest --no-daemon --stacktrace || status=$?
adb logcat -d -t 6000 > qa/instrumentation/logcat.txt 2>&1 || true
echo "Android connected tests exit status: $status"
exit "$status"
