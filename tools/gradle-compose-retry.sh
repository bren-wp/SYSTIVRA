#!/usr/bin/env bash
# SYSTIVRA by Brendigo
set -euo pipefail
# Recover only from a transient Kotlin Compose Gradle plugin-marker lookup.
# Compiler, lint, source, test and configuration errors remain terminal.
# Never convert an unsuccessful Gradle invocation into a green CI job.
if [[ "$#" -lt 1 ]]; then
  echo "Usage: bash tools/gradle-compose-retry.sh <Gradle tasks/options>" >&2
  exit 2
fi
log="$(mktemp "${RUNNER_TEMP:-/tmp}/systivra-gradle-plugin-retry.XXXXXX")"
trap 'rm -f "$log"' EXIT
max_attempts="${SYSTIVRA_COMPOSE_PLUGIN_ATTEMPTS:-4}"
if [[ ! "$max_attempts" =~ ^[1-4]$ ]]; then
  echo "::error::SYSTIVRA_COMPOSE_PLUGIN_ATTEMPTS must be between 1 and 4"
  exit 2
fi

for ((attempt=1; attempt<=max_attempts; attempt++)); do
  args=("$@" --no-daemon --stacktrace)
  if ((attempt > 1)); then
    # Gradle may have cached a transient negative plugin-marker lookup.
    args+=(--refresh-dependencies)
  fi
  echo "::group::Gradle invocation $attempt/$max_attempts"
  if gradle "${args[@]}" 2>&1 | tee "$log"; then
    echo "::endgroup::"
    exit 0
  else
    gradle_status="${PIPESTATUS[0]}"
  fi
  echo "::endgroup::"

  if ! grep -Fq "org.jetbrains.kotlin.plugin.compose" "$log" ||
     ! grep -Fq "was not found in any of the following sources" "$log"; then
    echo "::error::Gradle failed for a non-plugin-resolution reason; no automatic retry"
    exit "$gradle_status"
  fi
  if ((attempt == max_attempts)); then
    echo "::error::Compose plugin marker still unresolved after $attempt bounded attempts"
    exit "$gradle_status"
  fi
  # Bounded backoff: no retry of Kotlin compile errors or failing assertions.
  delay=$((attempt * 10))
  echo "::warning::Transient Compose plugin marker lookup failed ($attempt/$max_attempts); retry in ${delay}s"
  sleep "$delay"
done
exit 1
