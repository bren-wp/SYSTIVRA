# Changelog

## [Reference parity work in progress] — 2026-11-15

- Exported all original **37 design screens** into unmodified-layout 1080 × 1920 reference previews, contact sheet and downloadable ZIP on GitHub.
- Original 390 × 844 screen composition is preserved exactly in each preview; only the dark side margins are added to fit the 9:16 format.
- First layout matching pass for Android Dashboard, Hardware Explorer, 128dp SYSTIVRA ring card, 69dp list rows, cyan-purple accents and 62dp bottom navigation.
- Kept all features reachable through additional scrolling; no fabricated hardware readings added.
- Repaired malformed Android GitHub Actions upload step and restored CI execution; navigation smoke assertions follow updated layout.
- **Visual parity remains unverified**: these are reference concepts and not Google Play screenshots.

## [0.9.0-preview.1] — 2026-11-15

- Migrated **all** 24 Kotlin source and test files from the previous namespace into `com.brendigo.systivra`, including directory paths.
- Set Android `applicationId` and `namespace` exactly to `com.brendigo.systivra`; versionCode=10.
- Updated screenshots, marketing metadata and Google Play policies to the canonical Brendigo ID.
- Added `tools/validate_application_id.py` and mandatory CI pre-release checks to prevent accidental reversion.
- Upgraded **Gradle to 9.9.1**, addressing documented security vulnerabilities, and **kotlinx.coroutines to 1.11.0**.
- Kept latest available **stable** AGP 9.4.0, Kotlin 2.4.21, Compose BOM 2026.09.00, Activity 1.13.0, Core-KTX 1.19.1, Lifecycle 2.11.0, SplashScreen 1.2.0.
- Started authentic Android screenshot QA with updated package ID. Images are not claimed to be valid until verified against real application UI and Play requirements.
- **Breaking identity change**: Google Play considers the new application ID a new app; no automatic upgrade/data migration from the previous package.

## [0.8.0-preview.1] — 2026-11-15

- Updated **AGP 9.4.0** (Gradle 9.6.0 compatible) and **AndroidX SplashScreen 1.2.0** from published stable releases.
- Kept Kotlin 2.4.21, Compose BOM 2026.09.00, Activity 1.13.0, Lifecycle 2.11.0 and AndroidX Test 1.7.0 on verified stable versions.
- Added Dependabot maintenance for Gradle libraries and GitHub Actions.
- Added optional Usage Access disclosure and explicit action before opening Android Settings.
- Protected sensor discovery/registration/unregistration against vendor-specific runtime errors.
- Linked the in-app Privacy Center from About.
- Created a strict real-screenshot promotion gate: only unedited 1080×1920 emulator captures, no simulated screenshots, plus focus/duplicate checks.
- Android CI, lint, emulator capture and physical-device acceptance remain separate required quality gates; no crash-free or Google Play approval guarantee.

## [0.7.0-preview.1] — 2026-11-15

- Released editable SYSTIVRA Google Play icon and feature artwork plus exact-sized PNG assets in `brending/google-play/`, with reproducible generation workflow.
- Provided eight English-language promotional concept illustrations explicitly separated from real Play screenshots; no deceptive screenshot claim.
- Added English Play listing copy, target API / signing / permissions and Data Safety release-readiness checklist.
- Moved CSV and JSON document writing off the Compose UI thread to reduce stalls.
- Moved optional Android app usage queries off the main thread.
- Fixed the Screen Catalog route and clarified optional Usage Access privacy text.
- Hardened emulator screenshot QA: preserve native resolution, verify the SYSTIVRA process and focused app, reject invalid/repeated System UI screenshots, archive logcat evidence.
- **No zero-crash or Google Play approval claim**: physical-device, accessibility, full security and visual 1:1 tests remain open.

## [0.6.0-preview.1] — 2026-11-14

- Introduced **Session Insights** using measured battery charge and temperature with unavailable-safe calculations and a real historical chart.
- Added optional net battery percentage-point rate only for sufficiently long sample intervals, with explicit limitations.
- Implemented on-device JSON export alongside CSV using the system document picker.
- JSON v1 schema preserves explicit nulls, escapes special characters and excludes device identity.
- Added deterministic JUnit tests for trends, short intervals, unknown data and JSON encoding.
- Updated navigation, Recording Library, README and privacy documentation.
- Enhanced emulator screenshot QA inventory: 36 original app routes plus Recording Library and Session Insights. The system Splash requires a separate capture.
- **Pixel-perfect 1:1 is unverified**. No newly generated concept reference for Session Insights was claimed.

## [0.5.0-preview.1] — 2026-11-14

- Session-based SQLite schema v2 with non-destructive v1 data migration.
- Separate Recording Library: session list, recording dates and sample counts, session-specific analysis and CSV export.
- Confirmed deletion of an individual recorded session; retention stays capped at 900 actual samples total.
- Updated Android instrumented tests for database migration, reopen, deletion and empty sessions.
- Added manual vibration diagnostics with a user-confirmed result; VIBRATE is a normal permission.
- Fixed outdated emulator UI tests after Dashboard redesign and enabled programmatic scroll for off-screen navigation items.
- Updated public privacy copy, README and release documentation.
- **All 37 reference screens remain visually uncertified** until real emulator / physical-device screenshots are inspected and compared.

## [0.4.0-preview.1] — 2026-11-14

- Private SQLite persistence for user-initiated monitoring sessions, capped at 900 samples.
- Restored samples survive app restarts and can be cleared explicitly without root or storage permissions.
- Serialized database writes, restore on launch, ready state, and error visibility on recorder screen.
- Preserved field contracts for battery, thermal and free RAM in saved CSV exports and session summaries.
- SQLite reopen/clear instrumentation tests and reconstruction unit tests.
- Reconfigured emulator screenshot workflow after earlier ADB broken-pipe boot failure; API 33.
- Real Splash no longer mislabeled as a delayed Dashboard screenshot.
- All 37 pixel-perfect screen comparisons **remain unverified** pending actual screenshots and manual QA.

## [0.3.0-preview.2] — 2026-11-14

- Fixed nested in-app back-stack navigation.
- Updated native Dashboard with branded gradients, stronger device summary and category icons.
- Added gradient launcher splash artwork.
- Added Compose UI instrumentation smoke tests and emulator screenshot QA workflow.
- No 1:1 certification; manual screenshot and physical-device reviews remain outstanding.

## [0.3.0-preview.1] — 2026-11-14

### Added
- Fully routed set of 37 English-language concepts (system Splash + navigable in-app screens); functionality varies by module.
- Live sensors with units and metadata; Android network status without Internet access.
- Opt-in app usage reading, explicit Permission Center and local on-screen alert thresholds.
- Explicit foreground-only recording (900 samples), session statistics and manual CSV export.
- Bounded, cancellable CPU microbenchmark with thermal safety conditions and measured results.
- Real OpenGL ES GPU driver identification and optional CPU load from readable proc-stat deltas.
- Additional Android battery health, voltage and display refresh-rate information.
- In-app Performance HUD preview, not cross-app overlay functionality.
- Configurable sampling interval, user-confirmed diagnostic result persistence.
- Debug-only QA routes, automated emulator screenshot workflow and visual diff reporting.
- Deterministic proc-stat JUnit tests.

### Verification
GitHub Actions builds APK/AAB and executes unit tests; device and screenshot acceptance must be validated separately.

### Outstanding
Exact 1:1 visual parity remains unverified, permanent Room history and additional component tests are incomplete. Google Play release signing is not configured.

## [0.2.0-preview.1] — 2026-11-14

### Added
- SYSTIVRA product-marketing README with original brand graphics, technical feature descriptions, and visual design concept gallery.
- Live System Health: lists available readings instead of presenting a fabricated device health score.
- Privacy Center explaining the actual lack of network permissions and in-memory session history.
- CPU Core Inspector with transparent availability.
- Charging Analysis and full-sized battery chart using platform readings.
- CSV Reports & Export using Android's document picker; no storage permission required.
- JUnit tests for CSV formatting, UTC timestamps, missing metrics, and spreadsheet formula mitigation.
- Consistent Material navigation icons and dashboard battery activity chart.
- A 37-screen QA inventory and preview release pipeline producing versioned APK, unsigned AAB, source archive, and checksums.

### Improved
- README documentation and development status transparency.
- Android CI compiles debug APK, unsigned release AAB, and runs unit tests.
- All screens remain in English; Splash transitions directly into Dashboard.

### Known limitations
- Preview is not a production release; signed AAB and Play Console validation require private signing credentials.
- 1:1 visual match with 37 reference images is not yet measured or certified.
- Many advanced feature modules are planned, not implemented. No fabricated CPU/GPU readings, FPS, performance enhancement, or device health ratings.
- Session readings are not persisted after the process exits.
