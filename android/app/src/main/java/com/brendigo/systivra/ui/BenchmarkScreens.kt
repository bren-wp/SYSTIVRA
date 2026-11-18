package com.brendigo.systivra.ui

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class CpuBenchResult(
    val totalIterations: Long,
    val elapsedMs: Long,
    val checksum: Long,
    val thermalStatus: String,
    val stoppedEarly: Boolean
) {
    val iterationsPerSecond: Long =
        if (elapsedMs > 0) (totalIterations * 1000L) / elapsedMs else 0L
}

/**
 * A bounded, single-worker CPU microbenchmark, NOT comparable to established suites.
 * No persistent background execution. Cancels with composition scope.
 */
class BenchmarkController(context: Context) {
    private val power = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    var running by mutableStateOf(false)
        private set
    var result by mutableStateOf<CpuBenchResult?>(null)
        private set
    var message by mutableStateOf("Ready")
        private set
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        if (running) return
        running = true
        result = null
        message = "Running 2-second single-worker workload"
        job = scope.launch {
            try {
                result = withContext(Dispatchers.Default) { execute() }
                message = if (result?.stoppedEarly == true)
                    "Stopped early due to Android thermal conditions"
                else "Completed"
            } catch (canceled: CancellationException) {
                message = "Canceled by user"
            } catch (error: Exception) {
                message = "The benchmark could not complete. Please retry on a cooler device."
            } finally {
                running = false
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }

    private fun thermalStatus(): Int? =
        if (Build.VERSION.SDK_INT >= 29) {
            runCatching { power?.currentThermalStatus }.getOrNull()
        } else null

    private fun thermallyUnsafe() =
        thermalStatus()?.let { it >= PowerManager.THERMAL_STATUS_SEVERE } ?: false

    private suspend fun execute(): CpuBenchResult {
        val start = SystemClock.elapsedRealtimeNanos()
        var iterations = 0L
        var checksum = 1469598103934665603L
        var stoppedEarly = thermallyUnsafe()
        val deadline = start + 2_000_000_000L
        while (!stoppedEarly && SystemClock.elapsedRealtimeNanos() < deadline) {
            currentCoroutineContext().ensureActive()
            repeat(4096) { i ->
                checksum = (checksum xor i.toLong()) * 1099511628211L
            }
            iterations += 4096L
            if (iterations % 131072L == 0L) stoppedEarly = thermallyUnsafe()
        }
        val elapsed = ((SystemClock.elapsedRealtimeNanos() - start) / 1_000_000L).coerceAtLeast(1L)
        val thermal = thermalStatus()?.toString() ?: "Unavailable before API 29"
        return CpuBenchResult(iterations, elapsed, checksum, thermal, stoppedEarly)
    }
}

@Composable
fun BenchmarkCenterScreen(controller: BenchmarkController, onStart: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Performance Lab", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("A transparent local CPU microbenchmark. No online ranking or invented score.", color = Muted)
        }
        item { BenchCard("CPU single-worker test", "Deterministic integer workload for approximately 2 seconds.") }
        item { BenchCard("Safety", "Stops on severe Android thermal status and supports immediate cancellation. Avoid running on a hot device.") }
        item { Button(enabled = !controller.running, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = onStart) { Text("Start CPU test") } }
        if (controller.result != null) {
            item { BenchCard("Previous result", controller.result!!.iterationsPerSecond.toString() + " iterations/s") }
        }
        item { BenchCard("GPU, memory and storage benchmarks", "Not implemented in this preview; no simulated points are shown.") }
    }
}

@Composable
fun BenchmarkRunningScreen(controller: BenchmarkController, open: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("CPU Benchmark", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
        item { BenchCard("Status", controller.message) }
        item { BenchCard("Workload", "At most 2 seconds, single worker. Thermal cut-off: severe or above.") }
        if (controller.running) {
            item { Button(onClick = controller::cancel) { Text("Stop test") } }
        } else {
            item { Button(enabled = controller.result != null, colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Raised, disabledContentColor = Muted
                    ), onClick = { open("benchmark-results") }) { Text("View results") } }
            item { Button(onClick = { open("benchmark-center") }) { Text("Back to Performance Lab") } }
        }
    }
}

@Composable
fun BenchmarkResultsScreen(controller: BenchmarkController) {
    val r = controller.result
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Benchmark Results", fontWeight = FontWeight.Bold, fontSize = 24.sp) }
        if (r == null) {
            item { BenchCard("No completed test", "Start a CPU benchmark from Performance Lab.") }
        } else {
            item { BenchCard("Iterations / second", String.format(Locale.US, "%,d", r.iterationsPerSecond)) }
            item { BenchCard("Elapsed time", r.elapsedMs.toString() + " ms") }
            item { BenchCard("Total iterations", r.totalIterations.toString()) }
            item { BenchCard("Checksum", r.checksum.toString()) }
            item { BenchCard("Android thermal code", r.thermalStatus) }
            item { BenchCard("Test status", if (r.stoppedEarly) "Stopped due to thermal load" else "Completed") }
            item { Text("These numbers are local measurements only and cannot be compared to Geekbench or other suites.", color = Muted, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun BenchCard(title: String, details: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(details, color = Muted, fontSize = 13.sp)
        }
    }
}
