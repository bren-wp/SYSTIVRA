package com.brendigo.systivra

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.brendigo.systivra.ui.AppTheme
import com.brendigo.systivra.ui.Cyan
import com.brendigo.systivra.ui.Muted
import com.brendigo.systivra.ui.Panel
import com.brendigo.systivra.ui.Raised
import com.brendigo.systivra.ui.TextPrimary
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Effective Compose foregrounds, including dialog and interactive chip states. */
class ContrastControlsInstrumentedTest {
    @get:Rule val rule = createComposeRule()

    private fun contrast(a: Color, b: Color): Float {
        val l1 = a.luminance()
        val l2 = b.luminance()
        return (maxOf(l1, l2) + 0.05f) / (minOf(l1, l2) + 0.05f)
    }

    private fun readable(name: String, foreground: Color, surface: Color) {
        val ratio = contrast(foreground, surface)
        assertTrue(name + " is dark-on-dark: " + ratio, ratio >= 4.5f)
    }

    @Test fun cardInkAndFilledButtonLabelsStayReadable() {
        var cardInk = Color.Unspecified
        var buttonInk = Color.Unspecified
        rule.setContent {
            AppTheme {
                Column {
                    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
                        cardInk = LocalContentColor.current
                        Text("Dark card label")
                    }
                    Button(onClick = {}) {
                        buttonInk = LocalContentColor.current
                        Text("Bright cyan action")
                    }
                }
            }
        }
        rule.onNodeWithText("Dark card label").assertExists()
        rule.runOnIdle {
            readable("card", cardInk, Panel)
            readable("button", buttonInk, Cyan)
        }
    }

    @Test fun dialogHeadingBodyAndActionNeverRenderBlackOnDark() {
        var heading = Color.Unspecified
        var body = Color.Unspecified
        var action = Color.Unspecified
        rule.setContent {
            AppTheme {
                AlertDialog(
                    onDismissRequest = {},
                    containerColor = Raised,
                    titleContentColor = TextPrimary,
                    textContentColor = TextPrimary,
                    title = { heading = LocalContentColor.current; Text("Dialog heading") },
                    text = { body = LocalContentColor.current; Text("Dialog body") },
                    confirmButton = {
                        TextButton(onClick = {}) {
                            action = LocalContentColor.current
                            Text("Close")
                        }
                    }
                )
            }
        }
        rule.onNodeWithText("Dialog heading").assertExists()
        rule.onNodeWithText("Dialog body").assertExists()
        rule.runOnIdle {
            readable("dialog heading", heading, Raised)
            readable("dialog body", body, Raised)
            readable("dialog action", action, Raised)
        }
    }

    @Test fun selectedAndUnselectedChipsRemainReadableAndClickable() {
        var activeInk = Color.Unspecified
        var passiveInk = Color.Unspecified
        var chosen = false
        rule.setContent {
            AppTheme {
                var selected by remember { mutableStateOf(false) }
                chosen = selected
                FilterChip(
                    selected = selected,
                    onClick = { selected = !selected },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Panel,
                        labelColor = TextPrimary,
                        selectedContainerColor = Color(0xFF153B68),
                        selectedLabelColor = TextPrimary,
                        disabledContainerColor = Raised,
                        disabledLabelColor = Muted
                    ),
                    label = {
                        if (selected) activeInk = LocalContentColor.current
                        else passiveInk = LocalContentColor.current
                        Text("Select temperature")
                    }
                )
            }
        }
        rule.onNodeWithText("Select temperature").assertExists()
        rule.runOnIdle { readable("chip idle", passiveInk, Panel) }
        rule.onNodeWithText("Select temperature").performClick()
        rule.runOnIdle {
            assertTrue("chip click must change selected state", chosen)
            readable("chip selected", activeInk, Color(0xFF153B68))
        }
    }
}
