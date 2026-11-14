# Changelog

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
