# SYSTIVRA Android

Native Android foundation using Kotlin, Compose, and Android system APIs.

## Build
Install Android SDK Platform 37 and JDK 17. Use Gradle 9.6: from this directory run gradle :app:assembleDebug.

No APK has been locally built in this workspace. See the Android GitHub Actions job for compiler validation.

## Product
- Splash -> direct Dashboard (no account, no onboarding).
- English-only dark UI.
- Real battery, RAM, storage, Android thermal status, sensor inventory, device/OS properties and current in-memory monitoring history.
- No internet permission, advertising SDK, analytics SDK, paid upgrades or invented optimization scores.
- Display/touch self-test is manually verified.
- Restricted CPU/GPU metrics are honestly marked Unavailable.
- Other destinations exist in the development screen inventory but are explicitly labeled Planned.

See ../brending, ../docs/ROADMAP.md and ../docs/VISUAL-QA.md for design/QA requirements.
