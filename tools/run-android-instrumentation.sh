#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -uo pipefail
# Keep UI tests on their own fresh emulator, separate from 38 forced-route captures.
# A known Maven Central metadata outage is retried ONCE only if UTP's JUnit
# Platform artifacts could not be resolved. Never retry/mask a failing assertion.
mkdir -p qa/instrumentation
adb logcat -c >/dev/null 2>&1 || true

# A shard uses the standard Android Gradle instrumentation class filter.
# Omit this variable to run the full suite locally (never a silent skip).
runner_args=()
if [[ -n "${SYSTIVRA_TEST_CLASSES:-}" ]]; then
  runner_args+=("-Pandroid.testInstrumentationRunnerArguments.class=${SYSTIVRA_TEST_CLASSES}")
fi

run_connected_tests() {
  local output="$1"
  shift
  # An emulator/test-runner deadlock must terminate with exit code 124
  # and preserve diagnostics rather than silently consuming the entire job.
  if timeout --signal=TERM --kill-after=30s "${SYSTIVRA_TEST_TIMEOUT:-22m}" \
      gradle -p android :app:connectedDebugAndroidTest --no-daemon --stacktrace \
      "${runner_args[@]}" "$@" 2>&1 | tee "$output"; then
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
if [[ "$status" -eq 124 ]]; then
  echo "::error::Android instrumentation timed out; inspect this shard's Gradle log and logcat. No assertions were suppressed."
fi
echo "Android connected tests exit status: $status; class filter: ${SYSTIVRA_TEST_CLASSES:-ALL}"
exit "$status"
