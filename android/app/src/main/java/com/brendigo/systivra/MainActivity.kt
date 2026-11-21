package com.brendigo.systivra

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.brendigo.systivra.data.DeviceMetrics
import com.brendigo.systivra.data.DeviceSnapshot
import com.brendigo.systivra.data.RecordedSession
import com.brendigo.systivra.data.RecordingStore
import com.brendigo.systivra.ui.AppRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.brendigo.systivra.data.RecordingSampleGate

class MainActivity : ComponentActivity() {
    private sealed interface StorageAction {
        data object Start : StorageAction
        data object Stop : StorageAction
        data class Add(val snapshot: DeviceSnapshot) : StorageAction
        data object Clear : StorageAction
        data class Delete(val sessionId: Long) : StorageAction
        data class Select(val sessionId: Long?) : StorageAction
    }

    // A single IO consumer preserves control-action order. Individual hardware
    // snapshots are large: independently bound queued samples so a slow/full
    // SQLite database cannot exhaust app memory during a prolonged recording.
    // Start/stop/delete actions must never be silently dropped due to backpressure.
    private val actions = Channel<StorageAction>(Channel.UNLIMITED)
    private val pendingSampleWrites = RecordingSampleGate(maxPending = 32)

    private fun queueBoundedSample(snapshot: DeviceSnapshot) {
        if (!pendingSampleWrites.tryAcquire()) {
            storageError.value = "Recording storage is busy; some samples were skipped."
            return
        }
        if (actions.trySend(StorageAction.Add(snapshot)).isFailure) {
            pendingSampleWrites.release()
            storageError.value = "Recording storage is unavailable."
        }
    }
    private val storageReady = MutableStateFlow(false)
    private val storageError = MutableStateFlow<String?>(null)
    private val live = MutableStateFlow(DeviceSnapshot.empty())
    private val history = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val recorded = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val sessions = MutableStateFlow<List<RecordedSession>>(emptyList())
    private val selectedSessionId = MutableStateFlow<Long?>(null)
    private val selectedSamples = MutableStateFlow<List<DeviceSnapshot>>(emptyList())
    private val recording = MutableStateFlow(false)

    override fun onStop() {
        // Foreground-only recording: end an active session before leaving the screen.
        // The ordered storage actor processes queued samples before Stop.
        if (recording.value) {
            recording.value = false
            actions.trySend(StorageAction.Stop)
        }
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val metrics = DeviceMetrics(applicationContext)
        val store = RecordingStore(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            var activeId: Long? = null
            try {
            fun refresh() {
                recorded.value = store.readRecent()
                sessions.value = store.listSessions()
                selectedSamples.value = selectedSessionId.value?.let(store::readSession) ?: emptyList()
            }
            val loaded = runCatching {
                store.recoverInterruptedSessions()
                refresh()
            }
            if (loaded.isFailure) {
                storageError.value = "Saved history could not be opened"
            } else {
                storageReady.value = true
                for (action in actions) {
                    runCatching {
                        when (action) {
                            StorageAction.Start -> {
                                // A second Start must not orphan the previous active session.
                                activeId?.let(store::stopSession)
                                activeId = null
                            }
                            StorageAction.Stop -> {
                                activeId?.let(store::stopSession)
                                activeId = null
                            }
                            is StorageAction.Add -> {
                                val id = activeId ?: store.startSession(action.snapshot.time).also {
                                    activeId = it
                                }
                                store.insert(id, action.snapshot)
                            }
                            StorageAction.Clear -> {
                                store.clear()
                                selectedSessionId.value = null
                            }
                            is StorageAction.Delete -> {
                                if (activeId != action.sessionId) {
                                    store.deleteSession(action.sessionId)
                                    if (selectedSessionId.value == action.sessionId) {
                                        selectedSessionId.value = null
                                    }
                                }
                            }
                            is StorageAction.Select -> selectedSessionId.value = action.sessionId
                        }
                        refresh()
                        storageError.value = null
                    }.onFailure {
                        // Do not show SQLite exceptions, internal paths or
                        // provider implementation details in the user interface.
                        storageError.value = "Saved history action failed. Please retry."
                    }
                    if (action is StorageAction.Add) {
                        pendingSampleWrites.release()
                    }
                }
            }
            } finally {
                // The Activity scope may cancel before the queued Stop is consumed.
                // Seal the active session before closing the app-private database.
                runCatching { activeId?.let(store::stopSession) }
                runCatching { store.close() }
            }
        }

        val preferences = getSharedPreferences("systivra_settings", MODE_PRIVATE)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    runCatching {
                        withContext(Dispatchers.Default) { metrics.capture() }
                    }.onSuccess {
                        live.value = it
                        history.value = (history.value + it).takeLast(60)
                        if (recording.value && storageReady.value) {
                            queueBoundedSample(it)
                        }
                    }
                    delay(preferences.getInt("sampling_seconds", 2).coerceIn(1, 10) * 1_000L)
                }
            }
        }

        setContent {
            val snapshot = live.collectAsStateWithLifecycle().value
            val samples = history.collectAsStateWithLifecycle().value
            val saved = recorded.collectAsStateWithLifecycle().value
            val sessionList = sessions.collectAsStateWithLifecycle().value
            val session = selectedSessionId.collectAsStateWithLifecycle().value
            val selected = selectedSamples.collectAsStateWithLifecycle().value
            val isRecording = recording.collectAsStateWithLifecycle().value
            val ready = storageReady.collectAsStateWithLifecycle().value
            val writeError = storageError.collectAsStateWithLifecycle().value
            val qaRoute = if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
                intent?.getStringExtra("qa_route") else null

            AppRoot(
                snapshot = snapshot,
                samples = samples,
                recorded = saved,
                isRecording = isRecording,
                onToggleRecording = {
                    if (ready) {
                        val next = !recording.value
                        val submitted = actions.trySend(
                            if (next) StorageAction.Start else StorageAction.Stop
                        ).isSuccess
                        if (submitted) recording.value = next
                    }
                },
                onClearRecording = {
                    if (ready && !recording.value) actions.trySend(StorageAction.Clear)
                },
                initialRoute = qaRoute,
                historyReady = ready,
                historyStorageError = writeError,
                savedSessions = sessionList,
                selectedSessionId = session,
                selectedSessionSamples = selected,
                onSelectSession = {
                    if (ready) actions.trySend(StorageAction.Select(it))
                },
                onDeleteSession = {
                    if (ready && !recording.value) actions.trySend(StorageAction.Delete(it))
                }
            )
        }
    }
}
