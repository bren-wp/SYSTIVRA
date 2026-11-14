# SYSTIVRA roadmap & acceptance

| Phase | Deliverables | Status |
|---|---|---|
| 1 | Brand system, 37 design references, Android project, privacy-first responsive website | In progress |
| 2 | Real device sampling and detailed CPU/GPU/RAM/storage/battery/thermal/sensor screens | Partial foundation |
| 3 | Room session history, aggregation, export, alert thresholds | Planned |
| 4 | Honest component tests, benchmark methodology and safety cutoff | Planned |
| 5 | Optional overlay and advanced provider adapters | Planned |
| 6 | Full security/dead-code audit and screenshot diff QA | Planned |
| 7 | Tested signed release APK and AAB | Planned |

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
