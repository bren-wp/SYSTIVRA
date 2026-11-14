# SYSTIVRA

**Advanced Android Device Monitor & Diagnostics** — an English-first, privacy-focused Android application and accompanying website.

> **Development status:** early implementation. Screens under \`brending/screens/\` are **design mockups**, not screenshots from a running Android build. Metrics in those SVG design boards are illustrative only.

## Product rules

- **Splash → Dashboard**. No login, registration, onboarding, mandatory account, subscription or paid optimization claims.
- English-only interface; default dark theme.
- Real readings are annotated with source and availability. Unsupported hardware data is **Unavailable**, never fabricated.
- Local-first; no analytics, tracking, account collection or mandatory network access.
- Visual target: supplied SYSTIVRA design boards. Pixel-perfect status is **not yet validated** against emulator screenshots.

## Repository layout

| Folder | Purpose |
|---|---|
| \`android/\` | Kotlin / Jetpack Compose Android client |
| \`website/\` | Responsive static presentation website |
| \`brending/\` | SVG logo, app icon, 37 individually rendered vector design references and brand tokens |
| \`docs/\` | Architecture, capabilities, implementation status, visual QA and roadmap |

## Quick start

### Website
Open \`website/index.html\` in a browser, or host \`website/\` as static HTML/CSS/JS.

### Android
Open \`android/\` in Android Studio with a compatible JDK 17+ and Android SDK; sync Gradle and run the \`app\` target. Android build and instrumentation tests have not yet been executed in this environment. Inspect the GitHub Actions build check for verification when it finishes.

## Feature transparency

Included in the initial Android foundation: live battery %, battery temperature where reported, charging status, RAM and available storage, OS/device metadata, sensor inventory, Android thermal status where supported, core device hardware info, local in-memory metric history while on screen, dashboard and navigation.

Planned or partially implemented: GPU sampling, per-core frequencies on restricted systems, long-term Room recording, CPU/system workload estimation, benchmark lab, FPS overlay, exports, alert rules and diagnostics requiring privileges. The UI must indicate unavailable data rather than synthesize measurements.

## License and attribution

MIT — Copyright © 2026 BRENDIGO. Brand assets are project-original vector assets; external application code is **not copied** from competing Android apps.
