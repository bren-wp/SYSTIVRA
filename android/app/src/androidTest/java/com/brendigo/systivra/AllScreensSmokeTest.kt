package com.brendigo.systivra

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke-test the screen catalog with actual Android Activities.
 * This test does not assert exact pixels or every interactive control.
 *
 * Must be run on an emulator/device, not merely compiled by CI.
 */
@RunWith(AndroidJUnit4::class)
class AllScreensSmokeTest {
    private val routes = listOf(
        "dashboard", "hardware-explorer", "system-health", "privacy-center",
        "cpu-details", "cpu-cores", "gpu-details", "memory-details",
        "storage-details", "battery-details", "charging-analysis",
        "thermal-details", "display-details", "device-and-android",
        "network-details", "sensor-explorer", "sensor-live", "app-usage",
        "live-monitor", "metric-fullscreen", "recording-session",
        "session-analysis", "session-insights", "alerts",
        "diagnostics-center", "component-test", "diagnostic-results",
        "benchmark-center", "benchmark-running", "benchmark-results",
        "overlay-configuration", "history", "reports-export",
        "permission-center", "settings", "about", "device-detection",
        "system-overview", "recording-library", "all-screens",
        "component-test-interactive", "device-capacity",
        "cpu-cores-data", "app-usage-controls", "session-analysis-data",
        "benchmark-running-controls", "benchmark-results-data"
    )

    @Test
    fun everyRoutedScreenCanCreateAndResumeWithoutImmediateCrash() {
        val app = ApplicationProvider.getApplicationContext<android.content.Context>()
        for (route in routes) {
            val intent = Intent(app, MainActivity::class.java).apply {
                putExtra("qa_route", route)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            ActivityScenario.launch<MainActivity>(intent).use { scenario ->
                scenario.onActivity { activity ->
                    assertFalse("Route $route was destroyed", activity.isDestroyed)
                    assertFalse("Route $route was finishing", activity.isFinishing)
                }
            }
        }
    }
}
