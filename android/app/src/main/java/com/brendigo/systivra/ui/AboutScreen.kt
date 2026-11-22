// SYSTIVRA by Brendigo
package com.brendigo.systivra.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
    var showInfo by rememberSaveable { mutableStateOf(false) }
    fun openBrowser(url: String) {
        browserError = runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.isFailure
    }
    if (browserError) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { browserError = false },
            title = { Text("Browser unavailable") },
            text = { Text("Install a browser or open the SYSTIVRA project on GitHub.") },
            confirmButton = { TextButton(onClick = { browserError = false }) { Text("Close") } }
        )
    }
    if (showInfo) {
        AlertDialog(
            containerColor = Raised,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            onDismissRequest = { showInfo = false },
            title = { Text("SYSTIVRA by Brendigo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Version ${BuildConfig.VERSION_NAME} · MIT License")
                    Text("© 2026 Brendigo · Android system diagnostics")
                    Text("Offline-first. No account required. No invented device readings.", color = Muted)
                }
            },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Close") } }
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().testTag("about-page").verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReferenceHeading("About SYSTIVRA", "Version and licenses", "App information")
        ReferenceHeroCard { showInfo = true }
        Spacer(Modifier.height(0.dp))
        ReferenceMetricRow("Source code", "◇", 0) {
            openBrowser("https://github.com/bren-wp/SYSTIVRA")
        }
        ReferenceMetricRow("MIT license", "▤", 1) {
            openBrowser("https://github.com/bren-wp/SYSTIVRA/blob/main/LICENSE")
        }
        ReferenceMetricRow("Device support", "◉", 2) { open("device-and-android") }
        ReferenceMetricRow("Privacy", "⌁", 3) { open("privacy-center") }
    }
}
