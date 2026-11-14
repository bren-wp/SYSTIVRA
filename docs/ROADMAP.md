# SYSTIVRA roadmap & acceptance

| Phase | Deliverables | Status |
|---|---|---|
| 1 | Brand system, 37 design references, Android project, privacy-first responsive website | Implemented foundation |
| 2 | Public Android API metrics, EGL renderer, optional /proc load, live sensors and detail pages | Implemented where platform supports data |
| 3 | Private SQLite sessions, per-session analysis, deletion, CSV export and local threshold controls | Implemented baseline; richer analytics planned |
| 4 | Display/touch test and bounded CPU microbenchmark; additional component tests | Partial |
| 5 | In-app HUD preview; cross-app overlay and privileged hardware providers | Partial / latter not implemented |
| 6 | Visual evidence workflow and pixel-diff tool; full device/security/dead-code audit | In progress |
| 7 | Versioned debug APK, unsigned AAB, source archive and checksums | Preview workflow; production signing pending |

## Non-negotiable quality rules

- UI must match approved design references at measured, specified device resolutions; **no zero-difference claim without screenshots and image-diff reports**.
- Splash is the only launch gate; no account, signup or introductory carousel.
- English-only labels.
- No fake CPU, GPU, FPS, thermal or health data.
- Distinguish **measured**, **derived**, **estimated** and **not available**.
- Benchmark workloads must be bounded and stoppable.
- No online dependency for core capabilities.
- Release requires real hardware tests and successful CI. No release is claimed until APK/AAB exists.

## Required tests

Android 7 (API24), API29, API33, API36+; compact and large phone, tablet, landscape; 1x and enlarged fonts, dark/light system, TalkBack; charging and discharging, low memory, no sensors, no available battery current and denied optional permissions. Include screenshot tests and battery/performance profiling.
