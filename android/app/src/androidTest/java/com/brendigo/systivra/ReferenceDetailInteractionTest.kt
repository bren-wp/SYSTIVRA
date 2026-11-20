package com.brendigo.systivra

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.ReferenceDetailScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Verifies reference rows reveal a genuine Android reading rather than a dead UI control. */
class ReferenceDetailInteractionTest {
    @get:Rule val rule = createComposeRule()

    @Test fun batteryReferenceCardLeadsToMeasuredData() {
        rule.setContent {
            AppTheme {
                ReferenceDetailScreen(
                    route = "battery-details",
                    readings = listOf(Reading("Charge level", "77%", "ACTION_BATTERY_CHANGED")),
                    samples = emptyList(),
                    open = {}
                )
            }
        }
        rule.onNodeWithText("Power information").assertExists()
        rule.onNodeWithText("Charge level").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("selected-metric-battery-details")
            .assert(hasAnyDescendant(hasText("77%")))
        rule.onNodeWithTag("selected-metric-battery-details")
            .assert(hasAnyDescendant(hasText("Source: ACTION_BATTERY_CHANGED")))
    }

    @Test fun unavailableSensorDataIsNotInvented() {
        rule.setContent {
            AppTheme {
                ReferenceDetailScreen(
                    route = "sensor-explorer",
                    readings = listOf(Reading("Gyroscope", null, "SensorManager")),
                    samples = emptyList(),
                    open = {}
                )
            }
        }
        rule.onNodeWithText("Available sensors").assertExists()
        rule.onNodeWithText("Gyroscope").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("selected-metric-sensor-explorer")
            .assert(hasAnyDescendant(hasText("Unavailable")))
    }
    @Test fun selectedCpuCardShowsTheMappedReadingInsteadOfAnUnrelatedValue() {
        rule.setContent {
            AppTheme {
                ReferenceDetailScreen(
                    route = "cpu-details",
                    readings = listOf(
                        Reading("ABI", "arm64-v8a", "Build.SUPPORTED_ABIS"),
                        Reading("Available processors", "8", "Runtime")
                    ),
                    samples = emptyList(),
                    open = {}
                )
            }
        }
        rule.onNodeWithText("Architecture").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("selected-metric-cpu-details")
            .assert(hasAnyDescendant(hasText("arm64-v8a")))
        rule.onNodeWithTag("selected-metric-cpu-details")
            .assert(hasAnyDescendant(hasText("Source: Build.SUPPORTED_ABIS")))
    }

    @Test fun memoryHistoryCardNavigatesToRealSavedSessionScreen() {
        var destination: String? = null
        rule.setContent {
            AppTheme {
                ReferenceDetailScreen(
                    route = "memory-details",
                    readings = emptyList(),
                    samples = emptyList(),
                    open = { destination = it }
                )
            }
        }
        rule.onNodeWithText("History").performClick()
        rule.runOnIdle { assertEquals("history", destination) }
    }

    @Test fun referenceRowOpensQuickReadingWithoutForcedScroll() {
        rule.setContent {
            AppTheme {
                ReferenceDetailScreen(
                    route = "cpu-details",
                    readings = listOf(Reading("ABI", "arm64-v8a", "Android Build")),
                    samples = emptyList(), open = {}
                )
            }
        }
        rule.onNodeWithText("Architecture").performClick()
        rule.onNodeWithTag("selected-metric-cpu-details")
            .assert(hasAnyDescendant(hasText("arm64-v8a")))
        rule.onNodeWithText("Close").performClick()
        rule.onNodeWithTag("selected-metric-cpu-details").assertDoesNotExist()
    }

    @Test fun usersCanFilterTheDedicatedReadingScreenOffline() {
        rule.setContent {
            AppTheme {
                com.brendigo.systivra.ui.DeviceReadingsBrowser(
                    route = "battery-details",
                    readings = listOf(
                        Reading("Charge level", "77%", "Battery status"),
                        Reading("Voltage", "4.1 V", "Battery voltage")
                    )
                )
            }
        }
        rule.onNodeWithTag("metric-search-battery-details").performTextInput("voltage")
        rule.waitForIdle()
        rule.onNodeWithText("Voltage").assertExists()
        rule.onNodeWithText("4.1 V").assertExists()
        rule.onNodeWithText("Charge level").assertDoesNotExist()
    }

}
