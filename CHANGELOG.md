## Unreleased fixes (v0.12.0-preview.5)
- Reports SVG actions select and scroll to matching CSV/JSON/PDF/Sharesheet controls; added Compose emulator navigation checks.
- Split and bound Splash QA dependency installation for clearer failure diagnosis; retained the authentic Splash verification gate.

## v0.12.0-preview.9 — bounded navigation, local catalog filters, SQLite session cleanup (2026-11-21)
- Added five categories and All to the functional Screen Catalog, with compact 2×3 selection chips, offline combined search and reset for empty matches. All original reference-first layouts and Android safe areas remain unchanged.
- Replaced unbounded Compose saved navigation stack with cycle-aware, max-24-route history; primary bottom tabs still reset navigation. Added JVM tests for 250 route hops, revisits, tabs and back behavior.
- Atomically remove empty completed recording sessions at Stop and on DB reopen, while keeping active sessions and real saved readings unchanged. Added instrumented SQLite regression tests, preserving v1→v2 migration.
- Android `versionCode 23`, `versionName 0.12.0-preview.9`. Strict exact-SHA release gate includes native device interaction tests, visual/responsive QA, Splash, lint and artifact checksums. Physical-device visual 1:1 signoff and Play signing are not yet certified.

## v0.12.0-preview.8 — one task per screen, recording/permission usability (2026-11-21)
- Replaced four vertically stacked CSV/JSON/PDF/Sharesheet controls with a responsive 2×2 selectable format switcher and one corresponding export card; removed automatic report scroll-to-item. Original Reports SVG landing view unchanged.
- Added accessible no-samples state and direct Recording route; preserved per-format export outcomes and disabled empty exports.
- Removed forced scroll and 116dp filler from Recording Session and Permission Center; source-labeled quick results, real toggles, dedicated controls, saved recordings and storage error visibility remain.
- Added emulator tests for format switching/hidden inactive cards, empty-data recording navigation, quick Recording details and existing permission sources.
- Bumped Android versionCode to **22**, versionName to **0.12.0-preview.8**; exact-SHA Android lint/unit/emulator/visual/responsive/Splash release gates remain mandatory. Pixel-perfect 1:1 and Play signing are not certified.

## v0.12.0-preview.7 — focused functional screens and reduced scrolling (2026-11-20)
- Removed redundant 116dp vertical gaps and long secondary inline controls/readings from primary hardware, benchmark, sensor and privacy reference-overview routes. First reference hero/four-row layout remains.
- Quick Android measurement/result dialog on tap with actual source and optional copy, no automatic scroll-to-index. Preserved unavailability without fake values.
- Added nine own-purpose searchable Android metric browsers, accessible from corresponding overview, and maintained deep-linkable navigation routes.
- Reduced Dashboard and Settings to their main actions plus essential shortcuts (Hardware Explorer, feature catalog, About), preserving screen catalog search and complete navigation.
- Added emulator tests for no-scroll metric interaction, offline reading search, feature catalog search, persisted Fahrenheit conversion, and all nine real reading routes.
- versionCode **21**, versionName **0.12.0-preview.7**. Exact-SHA Android/emulator/visual/Splash/responsive release gate; original SVG references stay unchanged.

## v0.12.0-preview.6 — Android navigation, accessibility and native SVG luminance QA (2026-11-20)
- Maintained the reference 62dp application tab strip and 8dp safe gutter above Android's actual navigation controls, with a full-app Compose geometry regression.
- Exposed selected tabs through accessibility-correct Compose `selectable` semantics, with real UI interaction regressions.
- Fixed unrelated hero-card shortcuts on generic reference landing screens, now opening the relevant primary action.
- Added per-screen signed brightness deltas and an unambiguous app-only tab-strip color comparison against original SVGs; full-screen pixel diffs remain untouched.
- Bumped versionCode to **20**, versionName to **0.12.0-preview.6**. Exact-SHA lint, emulator, responsive, visual and Splash gates are mandatory; 1:1 and Play signing are not certified.

# Changelog

## v0.12.0-preview.5 — per-format export outcomes and CSV safety (2026-11-18)
- Fixed bottom navigation too close to Android gesture/three-button bar by reserving `WindowInsets.navigationBars`, adding an 8dp dark separation gutter and contrasting system navigation background, while keeping 62dp tab hit targets; expanded device-inset Compose regression test.

- Each CSV/JSON/PDF/Sharesheet operation now shows its export/cancel/error state in the correct card, not under unrelated CSV.
- Strengthened CSV spreadsheet-formula safeguards against BOM, invisible Unicode and control-code prefix characters; added JVM regressions for attack strings and normal readings.
- Fixed API-27-only windowLightNavigationBar in the API-24 base theme using a separate values-v27 override; Lint remains mandatory.
- Added one narrowly scoped Maven Central JUnit Platform dependency-resolution retry to connected Android test runner, keeping all test assertions and failure gates intact.
- VersionCode 19; release gated on full exact-SHA Android/emulator/visual/responsive/Splash QA. 1:1 approval and independent security audit remain outstanding.


## v0.11.0-preview.2 — measured pixel-grid refinements, 2026-11-16

- Corrected diagonal SVG gradient mapping across all app screens; aligned visible Android status/navigation bar colors with the dark source palette without masking native system UI.
- Matched shared hero's 88dp ring and source position, linear cyan-to-purple accent, and x=143 text anchor in the 390dp reference viewport.
- Aligned source action rows to 82dp pitch with 69dp hitboxes; adjusted row chevron and shared navigation-label offsets without removing 48dp-accessible touch surfaces.
- Added a real Android Compose geometry test for x/y positions, dimensions, 88dp ring, and exact 82dp row intervals.
- Bumped versionCode **14**, versionName **0.11.0-preview.2**. Requires real GitHub preview APK, unsigned AAB, full source and SHA256 checksums. **0/37 SVGs certified true pixel-perfect**; system UI and actual data differ.


## v0.11.0-preview.1 — five reference routes and accessibility, 2026-11-16

- Rebuilt **Metric Fullscreen**, **Privacy Center**, **System Health**, **Sensor Live View** and **Benchmark Center** with their original reference-first 390×844 headings, cards/charts and original action labels.
- Preserved all five operational screens in separately navigable detail routes. Real battery statistics, private data statements, Android sensor listeners and bounded CPU testing remain functional; unavailable benchmark values are not fabricated.
- Improved screen-reader heading identification, navigation-tab and reference-button roles, selected tab announcement, and large-font label overflow behavior.
- Increased Android versionCode to **13** and versionName to **0.11.0-preview.1**. A new GitHub preview is intended with a genuine developer-debug APK, unsigned release AAB, full source, marketing artwork, source SVG references and SHA-256 checksums.
- [Real 390×844 visual QA and instrumentation #43776782282](https://github.com/bren-wp/SYSTIVRA/actions/runs/43776782282): **36/36** original in-app image comparisons, **38** actual screen captures, **31/31** passing connected Android UI tests. Raw average changed pixels improved **60.568% → 58.912%**, RGB MAE **4.217%**. This is NOT a percent of subjective image quality and **0/37 original references are independently certified 1:1**.
- [Four responsive viewport captures #43776788020](https://github.com/bren-wp/SYSTIVRA/actions/runs/43776788020) and [true Splash cold-start #43776793860](https://github.com/bren-wp/SYSTIVRA/actions/runs/43776793860) also succeeded. Physical-device and complete accessibility acceptance remain pending.

## [Unreleased — PR #23 five high-difference references and device QA]

- Rebuilt **Benchmark Results, App Usage, Session Analysis, Benchmark Running, CPU Core Inspector** reference entry screens with original 390×844 headings, honest charts/hero and four interactive actions.
- Kept existing performance, UsageStats, session analytics, export and CPU inspection functions available through dedicated detail routes, with real Android values and explicit unsupported states; benchmark Stop retains actual cancellation.
- Added original-first-viewport metric mapping unit tests, Compose interaction tests and five full-functionality routes to crash/no-immediate-ANR screen smoke tests.
- Added a separate pre-launch Android system Splash video with extracted frames, native original SVG and manual-review contact sheet, without pretending a post-launch screenshot is a Splash.
- Added responsive QA at 360×800, 412×915 (130% font), 600×960 and 800×1280 with real focused-app captures and geometry/blank-image validation.
- No fabricated benchmark progress, CPU-core activity or scores. All 37 design references still require independent 1:1 certification.

## [Unreleased — PR #22 device metric interactions]

- Matched original first-viewport reference layout while connecting all nine hardware detail screens' metric rows to their correct real Android readings, sources and explicit unavailable states instead of a generic report jump.
- Added offline reading search by name, value or Android source; added local Clipboard copy actions without new permissions, below the design reference viewport.
- Added a measured used-RAM chart derived from Android MemoryInfo when the required samples exist, validating units, bounds, missing metrics and finite values; no synthetic chart points.
- Memory > History opens saved sessions; tapping one History session selects its correct ID before navigating to the private Recording Library.
- Added pure unit tests, Compose instrumented tests, and native 390×844 screenshot plus independent emulator CI coverage.
- The 37 design references are not all pixel-perfect certified; no release should be tagged without full QA and valid deliverables.

## [Unreleased — PR #20 native-viewport visual iteration]

- Split development into a new PR after #19 merged; no writes to the production main branch.
- Added independent native 390 × 844 SVG reference rendering and real-emulator screenshot capture, with enforced matching physical dimensions and a per-screen absolute image-diff report. Existing Play-resolution captures remain unchanged.
- Rebuilt CPU, GPU, Memory, Storage, Battery, Thermal, Display, Device & Android and Sensor Explorer first viewports using the source reference heading, hero or honest measured chart, and four clickable metric rows.
- Preserved actual Android readings, their source and explicit unavailable states beneath the reference layout. Chart lines only appear when enough real numeric samples exist.
- Added unit tests for the nine reference layout specifications, an Android test for functional reading navigation, and updated the CPU nested-navigation smoke test.
- Corrected a shell portability issue: the Android emulator runner uses POSIX sh; all bash-specific commands now execute in an explicit bash script.
- This iteration **does not certify 1:1 parity**. Native viewport screenshot and functional QA remain acceptance gates; original 37-screen certification is 0/37.

## [Unreleased — PR #19 visual parity and screen QA]

- Corrected transparent Compose foreground inheritance so headings remain light and legible across the midnight theme; added instrumented color regression coverage.
- Matched Hardware Explorer heading offset to the shared Dashboard / Monitor / Diagnostics reference grid.
- Rebuilt Settings and About entry screens around the original header, hero card and four reference rows, with working interval, privacy, device information, source-code and license controls.
- Kept fixed theme and device-provided units transparent rather than displaying fake toggles. Preserved existing Settings features below the reference section.
- Inspected genuine 38-screen Android emulator evidence, compared all 36 original in-app reference layouts and documented measured differences; no screen certified 1:1.
- Added a CSV-to-Markdown visual QA summary and removed CI auto-commit of unapproved Play screenshots.
- Existing v0.10.0-preview.2 release is unchanged; these are in-progress PR changes until new tests pass.

## [0.10.0-preview.2] — 2026-11-15

- Added irreversible-deletion confirmation for all saved recording sessions and samples, including a cancel-path Compose UI test.
- Explicitly stop foreground-only recordings when the activity leaves the screen; close SQLite store in a cancellation-safe finally block.
- Correct nested bottom-navigation selection and remove development inventory/stale persistence copy from the user interface.
- Incremented Android versionCode to 12. Existing v0.10.0-preview.1 release remains immutable.
- Actual emulator/device visual comparison, full functional tests, accessibility, security, and real-device validation are **still required**. No screen is certified 1:1.

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
