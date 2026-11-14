# Changelog

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
