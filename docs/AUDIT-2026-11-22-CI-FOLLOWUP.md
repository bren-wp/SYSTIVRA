# SYSTIVRA — 2026-11-22 evidence-based CI and recording follow-up

Scope: focused continuation of a much larger required audit; **not a full code, security, device or Google Play acceptance certificate**.

## Verified baseline

- `main`: `fdfadda2504d73ced9e8d7929a252f54432b95c9`.
- Published developer release: [v0.12.0-preview.12](https://github.com/bren-wp/SYSTIVRA/releases/tag/v0.12.0-preview.12). Its AAB is **unsigned**, not ready for Play submission.
- No open PRs at investigation start. Android build, responsive capture and Splash capture were successful on this SHA.
- [Native 390x844 viewport QA, run 44973870570](https://github.com/bren-wp/SYSTIVRA/actions/runs/44973870570): `reference-geometry` passed; `instrumented-ui-tests` was **cancelled**. These are different jobs; a passing screenshot job is not passing instrumentation.

## Reproduced failure evidence

- Instrumentation used one Android 33 emulator for all test classes, running `connectedDebugAndroidTest`.
- GitHub log shows graphics warnings `Failed to find ColorBuffer` and `Failed to bind to texture`; these are indicators, not proof that graphics alone caused the hang.
- The uploaded JUnit/logcat artifact has **zero parseable JUnit XML reports**. It includes partial per-test logs; the last test to begin was `ChargingReferenceInteractionTest.chartUsesOriginal170DpViewportAndFour69DpCards` at approximately 02:02 UTC. The job was cancelled at 02:43 UTC.
- Absence of JUnit evidence **must not count as success**. Possible root causes include an emulator/renderer deadlock or an individual Compose test hanging; sharding and a supported renderer are isolation measures, not yet a verified cure.

## Implemented changes in this follow-up

1. Move the 20 instrumented test classes into four non-overlapping matrix shards, each with a fresh emulator and independent JUnit/logcat artifact. `tools/check-instrumentation-shards.py` checks source-class coverage and duplicates before running.
2. Replace deprecated `-gpu swiftshader_indirect` with supported `-gpu swiftshader` **for instrumentation only**. Add a bounded test-run timeout, explicit timeout diagnostics and no blanket retries on failed assertions.
3. Add `--require-results` to JUnit summarization; lack of a parseable JUnit report is a CI failure rather than a silent pass.
4. Add a transactional existence/active-session guard before writing SQLite metric samples. Late writes after session Stop/Delete/Clear now fail rather than introducing orphan samples; add an instrumented regression test.
5. Replace the permissive 100% screenshot-diff thresholds with 4.5% MAE / 43% changed-pixel coarse safeguards and a per-screen baseline regression gate. Baseline values come from genuine 390x844 Android captures in run 44973870570 and are stored verbatim in `qa/baselines/native-390x844-preview12.csv`. Individual screens can grow by at most 0.8 percentage points MAE or 5 points changed pixels versus the recorded baseline.

## Quantified visual baseline (not a parity certificate)

- 36/36 in-app SVG boards compared to actual app screenshots at exactly 390x844, with a 160 dpi emulator override.
- Mean RGB absolute error: **2.653%**.
- Mean changed pixels: **34.985%**.
- Maximum single-screen RGB error: **3.438%** (Thermal Details).
- Maximum changed-pixel fraction: **36.644%** (System Health).
- Android system Splash requires separate evidence, and Recording Library / Session Insights do not have approved SVG equivalents.
- **0/37 original screens can be called independently certified 1:1.** Real readings, OS status bars and fonts remain sources of legitimate differences.
- Thresholds reject **regressions relative to the baseline**, not absolute visual mismatch; never edit the source SVG merely to pass.

## Release blockers

- Rerun all four test shards on the **same final PR commit** and examine exact JUnit pass/fail/skipped counts and logcat.
- If the charging geometry test stalls in its isolated shard, capture its timeout evidence and fix that specific test/runtime issue; do not skip or mark it optional.
- Run full static checks, device-version compatibility and physical-device/manual UI/accessibility checks.
- Review every screen's visual difference independently. The visual regression gate alone does not certify pixel parity.
- Produce SBOM/dependency results; verify actual store assets and policy declarations.
- Use the authorized existing Play upload key for a production-signed AAB. **Never generate or commit a replacement private keystore.**
- Current `targetSdk = 36` matches the Android 16 / API 36 requirement stated by the [official Google Play target API guide](https://developer.android.com/google/play/requirements/target-sdk); no other Play readiness claim follows from that fact.

Until these conditions are met, retain **developer preview / not verified for production** status, and do not publish a release from an unverified commit.
