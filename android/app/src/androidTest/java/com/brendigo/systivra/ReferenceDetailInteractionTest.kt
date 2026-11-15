package com.brendigo.systivra

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertDoesNotExist
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
        rule.onNodeWithText("77%").assertExists()
        rule.onNodeWithText("Source: ACTION_BATTERY_CHANGED").assertExists()
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
        rule.onNodeWithText("Unavailable").assertExists()
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
        rule.onNodeWithText("arm64-v8a").assertExists()
        rule.onNodeWithText("Source: Build.SUPPORTED_ABIS").assertExists()
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

    @Test fun usersCanFilterTheRealDeviceReadingListOffline() {
        rule.setContent {
            AppTheme {
                ReferenceDetailScreen(
                    route = "battery-details",
                    readings = listOf(
                        Reading("Charge level", "77%", "Battery status"),
                        Reading("Voltage", "4.1 V", "Battery voltage")
                    ),
                    samples = emptyList(),
                    open = {}
                )
            }
        }
        rule.onNodeWithTag("reference-detail-battery-details").performScrollToIndex(10)
        rule.onNodeWithTag("metric-search-battery-details").performTextInput("voltage")
        rule.waitForIdle()
        rule.onNodeWithText("Voltage").assertExists()
        rule.onNodeWithText("4.1 V").assertExists()
    }
}
