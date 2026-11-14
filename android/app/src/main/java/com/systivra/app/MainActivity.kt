package com.systivra.app

import android.os.Bundle
import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systivra.app.data.DeviceMetrics
import com.systivra.app.data.DeviceSnapshot
import com.systivra.app.data.RecordingStore
import kotlinx.coroutines.channels.Channel
import com.systivra.app.ui.AppRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private sealed interface StorageAction {
        data class Add(val snapshot: DeviceSnapshot) : StorageAction
        data object Clear : StorageAction
    }

    // Single consumer guarantees insert/clear operation order.
    private val storageActions = Channel<StorageAction>(Channel.UNLIMITED)
    private val storageReady = MutableStateFlow(false)
    private val storageError = MutableStateFlow<String?>(null)
    private val live = MutableStateFlow(DeviceSnapshot.empty())
    private val history = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val recorded = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val recording = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val metrics = DeviceMetrics(applicationContext)
        val store = RecordingStore(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            val loaded = runCatching { store.readRecent() }.getOrElse {
                storageError.value = "Local history could not be loaded"
                emptyList()
            }
            recorded.value = loaded
            storageReady.value = true
            for (action in storageActions) {
                runCatching {
                    when (action) {
                        is StorageAction.Add -> store.insert(action.snapshot)
                        StorageAction.Clear -> store.clear()
                    }
                }.onFailure { storageError.value = "Local history write failed" }
            }
            store.close()
        }
        val preferences = getSharedPreferences("systivra_settings", MODE_PRIVATE)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    runCatching { withContext(Dispatchers.Default) { metrics.capture() } }.onSuccess {
                        live.value = it
                        history.value = (history.value + it).takeLast(60)
                        if (recording.value && storageReady.value) {
                            recorded.value = (recorded.value + it).takeLast(RecordingStore.MAX_SAMPLES)
                            storageActions.trySend(StorageAction.Add(it))
                        }
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
            val ready = storageReady.collectAsStateWithLifecycle().value
            val writeError = storageError.collectAsStateWithLifecycle().value
            val qaRoute = if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
                intent?.getStringExtra("qa_route") else null
            AppRoot(snapshot, samples, recordedSamples, isRecording,
                onToggleRecording = {
                    if (ready) recording.value = !recording.value
                },
                onClearRecording = {
                    if (ready && !recording.value) {
                        recorded.value = emptyList()
                        storageActions.trySend(StorageAction.Clear)
                    }
                },
                initialRoute = qaRoute,
                historyReady = ready,
                historyStorageError = writeError
            )
        }
    }
}
