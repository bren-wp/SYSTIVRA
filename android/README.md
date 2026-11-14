# SYSTIVRA Android

Native Kotlin + Jetpack Compose, Android API 24+; English-only dark UI; Splash → direct Dashboard with no login or onboarding.

## Run tests and build

Use JDK 17+, Gradle 9.6.0 and Android SDK Platform 37.0:

    gradle :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease

The APK is a development **debug build**. The release AAB is **unsigned** until an authorized private signing key is supplied. Never commit a key or password.

## Implemented capabilities (v0.5.0-preview.1)

- Battery status, charge level, temperature, current if reported, Android-reported health and voltage.
- RAM and internal storage, refresh rate and system/device metadata.
- GPU renderer, vendor and OpenGL ES from a temporary EGL context.
- Optional system CPU activity from two readable /proc/stat snapshots; unavailable if the OS blocks access.
- Live SensorManager axes and units, unregistered when the user leaves that screen.
- Optional Usage Access statistics after Android system authorization.
- Network state without Internet access.
- User-controlled grouped SQLite sessions (max 900 total samples), per-session analysis, deletion and CSV export; the database stays private and is not backed up.
- Local on-screen battery/thermal alert thresholds, adjustable sampling interval.
- Two-second bounded CPU microbenchmark with cancel and Android thermal safeguard.
- User-confirmed display and touch diagnostic results.
- Customizable in-app HUD preview (not an overlay over other apps).
- Clear Unavailable states when restricted or unsupported.

## Known limitations

Some screens expose a limited subset of planned functionality. Expanded retention and analytics in Room, cross-app overlay, microphone/camera/speaker tests, and GPU/FPS/CPU temperature monitoring are not complete. Privacy and Google Play permission compliance must be validated before production distribution.

## 1:1 status

37 SVG/PNG visual concept references exist under brending/. Visual matching is **not yet certified**. Debug-only route extras support emulator screenshots; [GitHub screen QA](../.github/workflows/android-screenshots.yml) records image evidence and reports pixel differences. The app does not claim unverified 1:1 parity. SQLite integration is covered by targeted Android instrumentation tests.

See ../docs/SCREEN-STATUS.md and ../docs/VISUAL-QA.md.

## v0.5 Session migration

The SQLite schema is version 2. Its migration from v1 retains all existing metric samples and associates old recordings with a legacy session instead of clearing the database. New recordings receive independent session identifiers. An Android instrumentation test verifies migration and per-session deletion. The app still writes only while sampling in the foreground and still caps stored samples globally at 900.

## Diagnostic additions

A manual vibration pulse test requires only the normal Android VIBRATE permission, supports API 24+, and stores a **user-confirmed** result. It does not automatically classify hardware as functional.
