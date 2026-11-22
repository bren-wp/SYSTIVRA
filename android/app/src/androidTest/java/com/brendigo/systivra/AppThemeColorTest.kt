// SYSTIVRA by Brendigo
package com.brendigo.systivra

import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.material3.Text
import com.brendigo.systivra.ui.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AppThemeColorTest {
    @get:Rule val rule = createComposeRule()

    @Test fun transparentScreensUseReadableForeground() {
        var foreground = Color.Unspecified
        rule.setContent {
            AppTheme {
                foreground = LocalContentColor.current
                Text("Foreground contrast regression")
            }
        }
        rule.onNodeWithText("Foreground contrast regression").assertExists()
        rule.runOnIdle { assertEquals(Color(0xFFF4F8FC), foreground) }
    }
}
