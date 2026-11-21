#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -uo pipefail
# Keep UI tests on their own fresh emulator, separate from 38 forced-route captures.
# A known Maven Central metadata outage is retried ONCE only if UTP's JUnit
# Platform artifacts could not be resolved. Never retry/mask a failing assertion.
mkdir -p qa/instrumentation
adb logcat -c >/dev/null 2>&1 || true

run_connected_tests() {
  local output="$1"
  shift
  if gradle -p android :app:connectedDebugAndroidTest --no-daemon --stacktrace "$@" 2>&1 | tee "$output"; then
    return 0
  else
    return "${PIPESTATUS[0]}"
  fi
}

status=0
run_connected_tests qa/instrumentation/gradle-connected-first.log || status=$?
if [[ "$status" -ne 0 ]] &&
   grep -Fq "unified-test-platform-gradle-work-action" qa/instrumentation/gradle-connected-first.log &&
   grep -Eq 'Could not find org[.]junit[.]platform:junit-platform-(engine|launcher):1[.]13[.]3' qa/instrumentation/gradle-connected-first.log; then
  echo "::warning::JUnit Platform 1.13.3 UTP artifact resolution failed; retrying Maven Central lookup once. No UI assertions were suppressed."
  status=0
  run_connected_tests qa/instrumentation/gradle-connected-resolution-retry.log --refresh-dependencies || status=$?
fi

adb logcat -d -t 6000 > qa/instrumentation/logcat.txt 2>&1 || true
echo "Android connected tests exit status: $status"
exit "$status"
