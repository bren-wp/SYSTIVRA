# SYSTIVRA — dependency and Google Play compatibility audit
Date: 2026-11-15

## Verified stable dependencies

| Dependency | Selected stable version | Status |
|---|---|---|
| Android Gradle Plugin | 9.4.0 | Updated from 9.3.0, Gradle 9.6.0 required |
| Gradle | 9.6.0 | Already used by GitHub CI |
| Kotlin / Compose compiler plugin | 2.4.21 | Already stable |
| Android SDK compile | API 37 | Already supported by AGP 9.4 |
| Android SDK target | API 36 | Meets Google Play deadline August 31, 2026 |
| Android minimum | API 24 | Existing supported minimum |
| Compose BOM | 2026.09.00 | Already stable |
| AndroidX Activity Compose | 1.13.0 | Already stable |
| AndroidX Core KTX | 1.19.1 | Already stable |
| AndroidX Core SplashScreen | 1.2.0 | Updated from 1.0.1 |
| AndroidX Lifecycle | 2.11.0 | Existing stable; 2.12 only preview when checked |
| AndroidX Test Runner / Core | 1.7.0 | Existing stable; avoid 1.8 alpha |

## Maintenance policy

- Dependabot proposes upgrades in dedicated pull requests; all must pass CI before merge.
- Do not automatically promote alpha/preview AndroidX versions to production.
- Verify Gradle resolution, Kotlin compilation, unit tests, instrumentation compilation, lint and emulator tests.
- Avoid asserting zero crashes or total Google Play compliance without device testing and Play Console review.
- Do not commit release signing keys or Google Play credentials.
- Review transitive dependencies, SDK permissions and data handling for every update.

References:
- https://developer.android.com/build/releases/agp-9-4-0-release-notes
- https://developer.android.com/jetpack/androidx/releases/core
- https://developer.android.com/jetpack/androidx/releases/lifecycle
- https://developer.android.com/develop/ui/compose/bom
- https://support.google.com/googleplay/android-developer/answer/11926878
