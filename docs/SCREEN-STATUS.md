# SYSTIVRA screen-by-screen implementation and 1:1 QA matrix

**Product rule:** only Splash precedes Dashboard. All visible UI is English. No login, registration, pricing, onboarding or fake health/performance values.

References exported: **37**. Each PNG is a **concept rendering**, not a verified app screenshot.

| # | Screen reference | Android foundation | Pixel-perfect verified |
|---|---|---|---|
| 01 | [Splash](../brending/screens/01-splash.svg) | Foundation / partial | **No** |
| 02 | [System Health](../brending/screens/02-system-health.svg) | Foundation / partial | **No** |
| 03 | [Device Detection](../brending/screens/03-device-detection.svg) | Foundation / partial | **No** |
| 04 | [Privacy Center](../brending/screens/04-privacy-center.svg) | Foundation / partial | **No** |
| 05 | [Dashboard](../brending/screens/05-dashboard.svg) | Foundation / partial | **No** |
| 06 | [Hardware Explorer](../brending/screens/06-hardware-explorer.svg) | Foundation / partial | **No** |
| 07 | [Cpu Details](../brending/screens/07-cpu-details.svg) | Foundation / partial | **No** |
| 08 | [Cpu Cores](../brending/screens/08-cpu-cores.svg) | Foundation / partial | **No** |
| 09 | [Gpu Details](../brending/screens/09-gpu-details.svg) | Foundation / partial | **No** |
| 10 | [Memory Details](../brending/screens/10-memory-details.svg) | Foundation / partial | **No** |
| 11 | [Storage Details](../brending/screens/11-storage-details.svg) | Foundation / partial | **No** |
| 12 | [Battery Details](../brending/screens/12-battery-details.svg) | Foundation / partial | **No** |
| 13 | [Charging Analysis](../brending/screens/13-charging-analysis.svg) | Foundation / partial | **No** |
| 14 | [Thermal Details](../brending/screens/14-thermal-details.svg) | Foundation / partial | **No** |
| 15 | [Display Details](../brending/screens/15-display-details.svg) | Foundation / partial | **No** |
| 16 | [Device And Android](../brending/screens/16-device-and-android.svg) | Foundation / partial | **No** |
| 17 | [Network Details](../brending/screens/17-network-details.svg) | Design only; planned | **No** |
| 18 | [Sensor Explorer](../brending/screens/18-sensor-explorer.svg) | Foundation / partial | **No** |
| 19 | [Sensor Live](../brending/screens/19-sensor-live.svg) | Design only; planned | **No** |
| 20 | [App Usage](../brending/screens/20-app-usage.svg) | Design only; planned | **No** |
| 21 | [Live Monitor](../brending/screens/21-live-monitor.svg) | Foundation / partial | **No** |
| 22 | [Metric Fullscreen](../brending/screens/22-metric-fullscreen.svg) | Foundation / partial | **No** |
| 23 | [Recording Session](../brending/screens/23-recording-session.svg) | Design only; planned | **No** |
| 24 | [Session Analysis](../brending/screens/24-session-analysis.svg) | Design only; planned | **No** |
| 25 | [Alerts](../brending/screens/25-alerts.svg) | Design only; planned | **No** |
| 26 | [Diagnostics Center](../brending/screens/26-diagnostics-center.svg) | Foundation / partial | **No** |
| 27 | [Component Test](../brending/screens/27-component-test.svg) | Foundation / partial | **No** |
| 28 | [Diagnostic Results](../brending/screens/28-diagnostic-results.svg) | Design only; planned | **No** |
| 29 | [Benchmark Center](../brending/screens/29-benchmark-center.svg) | Design only; planned | **No** |
| 30 | [Benchmark Running](../brending/screens/30-benchmark-running.svg) | Design only; planned | **No** |
| 31 | [Benchmark Results](../brending/screens/31-benchmark-results.svg) | Design only; planned | **No** |
| 32 | [Overlay Configuration](../brending/screens/32-overlay-configuration.svg) | Design only; planned | **No** |
| 33 | [History](../brending/screens/33-history.svg) | Foundation / partial | **No** |
| 34 | [Reports Export](../brending/screens/34-reports-export.svg) | Foundation / partial | **No** |
| 35 | [Permission Center](../brending/screens/35-permission-center.svg) | Design only; planned | **No** |
| 36 | [Settings](../brending/screens/36-settings.svg) | Foundation / partial | **No** |
| 37 | [About](../brending/screens/37-about.svg) | Foundation / partial | **No** |


## Validation requirements

1. On Android devices capture screenshots for each implemented route at 360×800, 390×844, 412×915, 600×960 and 800×1280, with controlled densities, font scales, OS version and insets.
2. Compare against approved reference renders with the [pixel difference script](../tools/visual-diff.py); archive the reference, actual screenshot, visual diff, mean error and changed-pixel percentage.
3. Retain a route-to-reference mapping and verify functionality and accessibility independently of pixel similarity.
4. Resolve card dimensions, elevation, typography, icon shapes, safe area, status/navigation bars, themes, landscape and large screens.
5. **Never claim a 1:1 match** without automated comparison plus manual on-device review.

**Current result:** 0/37 screens certified pixel-perfect. Screens partially implemented do not equal finished functional modules.
