# SYSTIVRA — dependency and Google Play compatibility audit
Originally 2026-11-15; reviewed 2026-11-21

## Verified stable dependencies

| Dependency | Selected stable version | Status |
|---|---|---|
| Android Gradle Plugin | 9.5.1 | Current pinned source version; CI runs Gradle 9.9.1, compatibility verified by build |
| Gradle | 9.9.1 | Updated for November 2026 critical/high security fixes; CI validation pending |
| Kotlin / Compose compiler plugin | 2.4.21 | Latest stable as of November 2026 |
| Android SDK compile | API 37 | Already supported by AGP 9.4 |
| Android SDK target | API 36 | Meets Google Play deadline August 31, 2026 |
| Android minimum | API 24 | Existing supported minimum |
| Compose BOM | 2026.11.01 | November 19, 2026 stable Google Maven BOM; CI validation required |
| AndroidX Activity Compose | 1.13.0 | Already stable |
| AndroidX Core KTX | 1.19.1 | Already stable |
| AndroidX Core SplashScreen | 1.2.0 | Updated from 1.0.1 |
| AndroidX Lifecycle | 2.11.1 | November 18, 2026 stable patch; CI validation required |
| AndroidX Test Runner / Core | 1.7.0 | Existing stable; avoid 1.8 alpha |

## Maintenance policy

- Dependabot proposes upgrades in dedicated pull requests; all must pass CI before merge.
- Do not automatically promote alpha/preview AndroidX versions to production.
- Verify Gradle resolution, Kotlin compilation, unit tests, instrumentation compilation, lint and emulator tests.
- Avoid asserting zero crashes or total Google Play compliance without device testing and Play Console review.
- Do not commit release signing keys or Google Play credentials.
- Review transitive dependencies, SDK permissions and data handling for every update.

References:
- https://developer.android.com/build/releases/agp-9-5-0-release-notes
- https://developer.android.com/jetpack/androidx/releases/core
- https://developer.android.com/jetpack/androidx/releases/lifecycle
- https://developer.android.com/develop/ui/compose/bom
- https://support.google.com/googleplay/android-developer/answer/11926878

## Verified November 15 updates

- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0`: newest stable release, upgraded from 1.10.2. [Kotlin coroutines releases](https://github.com/Kotlin/kotlinx.coroutines/releases).
- Gradle `9.9.1`: patched documented security vulnerabilities in version catalog accessors and Gradle daemon registry; upgraded CI from 9.6.0. [Gradle 9.9.1 release notes](https://docs.gradle.org/current/release-notes.html).
- Stable Compose BOM `2026.09.00` (superseded on Nov 21), AndroidX Activity `1.13.0`, Lifecycle `2.11.0` (superseded on Nov 21), AndroidX Test `1.7.0` kept stable; newer versions of some libraries are pre-release and intentionally not used.
- Verify exact application ID by running `python3 tools/validate_application_id.py` before any release.
- New Android Play app ID: `com.brendigo.systivra`. This is a new Play listing, not an update to a previous ID.


## November 18 source-level security check

- Verified `android/build.gradle.kts` pins **AGP 9.5.1** and **Kotlin 2.4.21**; Android CI pins **Gradle 9.9.1**. AGP 9.5.1 is documented on the official AGP 9.5 notes page. Gradle 9.9.1 contains fixes for three published build-system vulnerabilities; do not regress to Gradle 9.9.0. See https://docs.gradle.org/9.9.1/release-notes.html .
- Verified runtime dependencies are literal, pinned versions or version-catalog-free Compose BOM entries. No dynamic `+`, `SNAPSHOT`, or auto-changing dependency constraints were found in the reviewed Android Gradle file.
- No INTERNET permission, ads or external analytics SDK appears in the current app manifest and dependency declarations.
- **Not independently verified**: complete transitive Maven dependency graph, all resolved artifacts against a timestamped vulnerability database, signed dependency supply-chain provenance, and current Play Console policy state. A source/version review cannot establish that no known CVE exists anywhere in the full dependency graph.
- Dependabot tracks Gradle and GitHub Actions weekly; production release must add a reproducible SBOM, dependency advisory scan and risk triage before claiming a full dependency-security pass.

## November 21 current stable dependency refresh

- Updated app Compose BOM **2026.09.00 → 2026.11.01** (Google Maven release dated Nov 19, 2026). This aligns current Compose runtime/foundation modules with November stable releases rather than introducing alpha modules.
- Updated AndroidX Lifecycle runtime KTX and runtime Compose **2.11.0 → 2.11.1** (Nov 18 stable patch).
- Retained Kotlin **2.4.21** rather than the November Kotlin 2.5 release candidate, and AGP **9.5.1** rather than a prerelease. Activity Compose **1.13.0**, AndroidX Core **1.19.1**, AndroidX SplashScreen **1.2.0** and coroutines **1.11.0** remain pinned until a strictly newer stable version and compatibility are verified.
- These changes require Gradle resolution, lint/JVM/device tests and visual native comparison. A successful build does **not** replace transitive dependency SBOM/CVE review.

Sources:
- https://developer.android.com/jetpack/androidx/versions/stable-channel
- https://developer.android.com/jetpack/androidx/releases/lifecycle
- https://mvnrepository.com/artifact/androidx.compose/compose-bom/2026.11.01
- https://kotlinlang.org/docs/releases.html
