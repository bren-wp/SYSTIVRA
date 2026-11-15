package com.brendigo.systivra

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.BenchmarkController
import com.brendigo.systivra.ui.NextPriorityReferenceScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NextPriorityInteractionTest {
    @get:Rule val rule = createComposeRule()
    private val context get() = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val empty get() = DeviceSnapshot.empty()

    @Test fun metricFullscreenShowsCorrectReferenceRowsAndRealMinimum() {
        val measurements = listOf(38, 65).mapIndexed { i, level ->
            empty.copy(time = i.toLong() + 1L, battery = listOf(
                Reading("Charge level", "$level%", "Battery broadcast")
            ))
        }
        rule.setContent {
            AppTheme {
                NextPriorityReferenceScreen(
                    "metric-fullscreen", empty, measurements,
                    BenchmarkController(context), onStartBenchmark = {}, open = {}
                )
            }
        }
        rule.onNodeWithText("Detailed timeline").assertExists()
        rule.onNodeWithText("Minimum").performClick()
        rule.onNodeWithTag("next-selected-metric-fullscreen")
            .assert(hasAnyDescendant(hasText("38.0%")))
    }

    @Test fun privacyScreenOpensOnlyOptionalPermissionPage() {
        var destination: String? = null
        rule.setContent {
            AppTheme {
                NextPriorityReferenceScreen(
                    "privacy-center", empty, emptyList(),
                    BenchmarkController(context), onStartBenchmark = {},
                    open = { destination = it }
                )
            }
        }
        rule.onNodeWithText("Privacy and control").assertExists()
        rule.onNodeWithText("Optional Android access").performClick()
        rule.runOnIdle { assertEquals("permission-center", destination) }
    }

    @Test fun healthCardOpensActualBatteryDetails() {
        var destination: String? = null
        rule.setContent {
            AppTheme {
                NextPriorityReferenceScreen(
                    "system-health", empty, emptyList(),
                    BenchmarkController(context), onStartBenchmark = {},
                    open = { destination = it }
                )
            }
        }
        rule.onNodeWithText("Your system at a glance").assertExists()
        rule.onNodeWithText("Battery status").performClick()
        rule.runOnIdle { assertEquals("battery-details", destination) }
    }

    @Test fun sensorAxisOpensActualSensorManagerListenerControls() {
        var destination: String? = null
        rule.setContent {
            AppTheme {
                NextPriorityReferenceScreen(
                    "sensor-live", empty, emptyList(),
                    BenchmarkController(context), onStartBenchmark = {},
                    open = { destination = it }
                )
            }
        }
        rule.onNodeWithText("Sensor timeline").assertExists()
        rule.onNodeWithText("X axis").performClick()
        rule.runOnIdle { assertEquals("sensor-live-controls", destination) }
    }

    @Test fun benchmarkOnlyStartsExplicitCpuTest() {
        var starts = 0
        rule.setContent {
            AppTheme {
                NextPriorityReferenceScreen(
                    "benchmark-center", empty, emptyList(),
                    BenchmarkController(context),
                    onStartBenchmark = { starts++ }, open = {}
                )
            }
        }
        rule.onNodeWithText("Performance tests").assertExists()
        rule.onNodeWithText("Memory test").performClick()
        rule.onNodeWithTag("next-selected-benchmark-center")
            .assert(hasAnyDescendant(hasText("Unavailable")))
        rule.runOnIdle { assertEquals(0, starts) }
        rule.onNodeWithTag("next-reference-benchmark-center")
            .performScrollToIndex(3)
        rule.onNodeWithText("CPU test").performClick()
        rule.runOnIdle { assertEquals(1, starts) }
    }
}
