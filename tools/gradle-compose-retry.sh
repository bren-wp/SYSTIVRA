#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# Only retry a failed Gradle invocation when the *specific* Compose plugin
# marker lookup failed. Compiler errors, lint failures, assertion failures and
# configuration errors are terminal. No hidden success or skipped checks.
if [[ "$#" -lt 1 ]]; then
  echo "Usage: bash tools/gradle-compose-retry.sh <Gradle tasks/options>" >&2
  exit 2
fi
log="$(mktemp "${RUNNER_TEMP:-/tmp}/systivra-gradle-plugin-retry.XXXXXX")"
trap 'rm -f "$log"' EXIT
if gradle "$@" --no-daemon --stacktrace 2>&1 | tee "$log"; then
  exit 0
fi
if grep -Fq "org.jetbrains.kotlin.plugin.compose" "$log" &&
   grep -Fq "was not found in any of the following sources" "$log"; then
  echo "::warning::Compose plugin resolution failed; trying exactly one refreshed dependency lookup"
  gradle "$@" --refresh-dependencies --no-daemon --stacktrace
else
  echo "::error::Gradle failed for a non-plugin-resolution reason; original failure is above"
  exit 1
fi
