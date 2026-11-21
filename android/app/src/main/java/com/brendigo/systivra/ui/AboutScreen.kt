// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.systivra.BuildConfig

@Composable
internal fun Planned(title: String) {
    Box(Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
        PanelCard {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Text("This screen is unavailable.", color = Muted)
            Text("No measurements or test results are displayed.", color = Muted)
        }
    }
}

@Composable
internal fun About(open: (String) -> Unit) {
    val context = LocalContext.current
    var browserError by remember { mutableStateOf(false) }
    fun openBrowser(url: String) {
        browserError = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.isFailure
    }

    LazyColumn(
        modifier = Modifier.testTag("about-scroll"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ReferenceHeading("About SYSTIVRA", "Version and licenses", "Open development") }
        item { ReferenceHeroCard { open("device-and-android") } }
        item { Spacer(Modifier.height(0.dp)) }
        item { ReferenceMetricRow("Source code", "◇", 0) { openBrowser("https://github.com/bren-wp/SYSTIVRA") } }
        item { ReferenceMetricRow("MIT license", "▤", 1) { openBrowser("https://github.com/bren-wp/SYSTIVRA/blob/main/LICENSE") } }
        item { ReferenceMetricRow("Device support", "◉", 2) { open("device-and-android") } }
        item { ReferenceMetricRow("Privacy", "⌁", 3) { open("privacy-center") } }
        if (browserError) {
            item { Text("No browser could open the link. Check that a web browser is installed.", color = Muted) }
        }
        item { Spacer(Modifier.height(116.dp)) }
        item {
            PanelCard {
                Text("SYSTIVRA by BRENDIGO", color = Cyan, fontWeight = FontWeight.Bold)
                Text("Version ${BuildConfig.VERSION_NAME} · MIT License · © 2026 BRENDIGO", fontSize = 12.sp, color = Muted)
                Text("Privacy-first Android system monitor and diagnostics", color = Muted)
            }
        }
        item {
            PanelCard {
                Text("Transparent measurements", fontWeight = FontWeight.Bold)
                Text("Unsupported CPU or GPU temperatures and other restricted Android values are shown as Unavailable.", color = Muted)
            }
        }
    }
}
