package com.systivra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systivra.app.data.DeviceMetrics
import com.systivra.app.data.DeviceSnapshot
import com.systivra.app.ui.AppRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val live = MutableStateFlow(DeviceSnapshot.empty())
    private val history = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val recorded = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val recording = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val metrics = DeviceMetrics(applicationContext)
        val preferences = getSharedPreferences("systivra_settings", MODE_PRIVATE)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    runCatching { withContext(Dispatchers.Default) { metrics.capture() } }.onSuccess {
                        live.value = it
                        history.value = (history.value + it).takeLast(60)
                        if (recording.value) recorded.value = (recorded.value + it).takeLast(900)
                    }
                    delay(preferences.getInt("sampling_seconds", 2).coerceIn(1, 10) * 1_000L)
                }
            }
        }
        setContent {
            val snapshot = live.collectAsStateWithLifecycle().value
            val samples = history.collectAsStateWithLifecycle().value
            val recordedSamples = recorded.collectAsStateWithLifecycle().value
            val isRecording = recording.collectAsStateWithLifecycle().value
            AppRoot(snapshot, samples, recordedSamples, isRecording,
                onToggleRecording = { recording.value = !recording.value },
                onClearRecording = { if (!recording.value) recorded.value = emptyList() }
            )
        }
    }
}
