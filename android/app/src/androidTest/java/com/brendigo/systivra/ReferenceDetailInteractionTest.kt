package com.brendigo.systivra

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.data.Reading
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.ReferenceDetailScreen
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
}
