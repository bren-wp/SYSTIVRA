# SYSTIVRA architecture

The Android foundation uses Kotlin, Jetpack Compose and platform-backed metric providers. The sampling layer reads a single snapshot on a bounded foreground cadence and only while the UI is visible. This avoids pretending that Android gives every third-party app access to CPU/GPU thermometers or arbitrary FPS monitoring.

## Current data contract

\`Metric<T>\`: nullable value, display unit, data source, availability and timestamp. A missing value is **not** zero. The dashboard should show \`Unavailable\` with explanatory context.

### Supported sources
- \`ActivityManager.MemoryInfo\`: total/available RAM.
- \`BatteryManager\` and \`ACTION_BATTERY_CHANGED\`: level, state, battery temperature if supplied, current if supported.
- \`StatFs\`: available/total internal app-accessible storage.
- \`Build\`, \`DisplayMetrics\`: OS/device basics.
- \`SensorManager\`: list available sensors, not permissions for sensitive readings.
- \`PowerManager.currentThermalStatus\` on API29+.

### Constraints
System-wide CPU utilization, per-core clocks, GPU load, CPU/GPU temperature and app-level FPS overlay are hardware and Android-version dependent. Values must not be inferred from illustrative branding boards.

## Modules
Current initial setup is a minimal Android app module; the roadmap targets feature modules as implementation matures. Do not create empty modules solely for appearance.

## References
- https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
- https://developer.android.com/reference/android/os/BatteryManager
- https://developer.android.com/reference/android/os/PowerManager
- https://developer.android.com/reference/android/hardware/SensorManager
