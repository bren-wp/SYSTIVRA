package com.brendigo.systivra.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import java.util.Locale

data class Reading(val name: String, val value: String?, val source: String) {
    val display: String get() = value ?: "Unavailable"
}
data class DeviceSnapshot(
    val time: Long,
    val name: String,
    val cpu: List<Reading>,
    val gpu: List<Reading>,
    val ram: List<Reading>,
    val storage: List<Reading>,
    val battery: List<Reading>,
    val thermal: List<Reading>,
    val display: List<Reading>,
    val system: List<Reading>,
    val sensors: List<Reading>
) {
    companion object {
        fun empty(): DeviceSnapshot = DeviceSnapshot(
            0L, "Detecting device", emptyList(), emptyList(), emptyList(),
            emptyList(), emptyList(), emptyList(), emptyList(), emptyList(),
            emptyList()
        )
    }
}
class DeviceMetrics(context: Context) {
    private val app = context.applicationContext
    private val activityManager = app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val batteryManager = app.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val powerManager = app.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val cpuSampler = CpuSampler()
    private val detectedGpu by lazy { GpuProbe.read() }

    fun capture(): DeviceSnapshot {
        val memory = runCatching {
            ActivityManager.MemoryInfo().also { info -> activityManager?.getMemoryInfo(info) }
        }.getOrNull()
        val storage = runCatching { StatFs(Environment.getDataDirectory().absolutePath) }.getOrNull()
        val intent = runCatching {
            app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()
        val charge = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val level = if (charge >= 0 && scale > 0) decimal(charge * 100.0 / scale) + "%" else null
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE && it > -1000 }?.let { decimal(it / 10.0) + " °C" }
        val charging = when (intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> null
        }
        val microAmps = runCatching {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        }.getOrNull()?.takeUnless { it == Int.MIN_VALUE }
        val thermalState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (runCatching { powerManager?.currentThermalStatus }.getOrNull()) {
                PowerManager.THERMAL_STATUS_NONE -> "None"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
                else -> "Unknown"
            }
        } else null
        val dm = app.resources.displayMetrics
        val reportedHz = runCatching {
            (app.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager)
                ?.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate
        }.getOrNull()
        val health = when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good (Android reported)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
            else -> null
        }
        val millivolts = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            ?.takeIf { it > 0 }
        val cpuLoad = cpuSampler.samplePercent()
        val detectedSensors = runCatching {
            sensorManager?.getSensorList(Sensor.TYPE_ALL).orEmpty()
        }.getOrDefault(emptyList())
        val name = listOf(Build.MANUFACTURER, Build.MODEL).filter { !it.isNullOrBlank() }.joinToString(" ")
        return DeviceSnapshot(
            time = System.currentTimeMillis(),
            name = name,
            cpu = listOf(
                Reading("SoC", if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL?.takeIf(String::isNotBlank) else null, "Build.SOC_MODEL"),
                Reading("ABI", Build.SUPPORTED_ABIS.firstOrNull(), "Build.SUPPORTED_ABIS"),
                Reading("Available processors", Runtime.getRuntime().availableProcessors().toString(), "Runtime.availableProcessors"),
                Reading("System-wide CPU load", cpuLoad, "/proc/stat delta (only when readable)"),
                Reading("CPU temperature", null, "Not available via standard Android API")
            ),
            gpu = detectedGpu,
            ram = listOf(
                Reading("Total RAM", memory?.totalMem?.takeIf { it > 0L }?.let(::bytes), "ActivityManager.MemoryInfo"),
                Reading("Available RAM", memory?.availMem?.takeIf { it >= 0L && memory.totalMem > 0L }?.let(::bytes), "ActivityManager.MemoryInfo"),
                Reading("Used RAM", memory?.takeIf { it.totalMem > 0L }?.let { bytes((it.totalMem - it.availMem).coerceAtLeast(0L)) }, "Derived from ActivityManager"),
                Reading("Low memory", memory?.takeIf { it.totalMem > 0L }?.let { if (it.lowMemory) "Yes" else "No" }, "ActivityManager.MemoryInfo")
            ),
            storage = listOf(
                Reading("Total internal storage", storage?.totalBytes?.let(::bytes), "StatFs"),
                Reading("Available storage", storage?.availableBytes?.let(::bytes), "StatFs"),
                Reading("Used storage", storage?.let { bytes((it.totalBytes - it.availableBytes).coerceAtLeast(0L)) }, "Derived from StatFs")
            ),
            battery = listOf(
                Reading("Charge level", level, "ACTION_BATTERY_CHANGED"),
                Reading("Charging status", charging, "ACTION_BATTERY_CHANGED"),
                Reading("Battery temperature", temp, "ACTION_BATTERY_CHANGED"),
                Reading("Battery current", microAmps?.let { decimal(it / 1000.0) + " mA" }, "BatteryManager"),
                Reading("Android-reported health", health, "ACTION_BATTERY_CHANGED"),
                Reading("Voltage", millivolts?.let { decimal(it / 1000.0) + " V" }, "ACTION_BATTERY_CHANGED"),
                Reading("Technology", intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf(String::isNotBlank), "ACTION_BATTERY_CHANGED")
            ),
            thermal = listOf(
                Reading("Thermal status", thermalState, "PowerManager (API 29+)"),
                Reading("Battery temperature", temp, "ACTION_BATTERY_CHANGED"),
                Reading("CPU/GPU thermometer", null, "Restricted / device dependent")
            ),
            display = listOf(
                Reading("Logical resolution", dm.widthPixels.toString() + " × " + dm.heightPixels.toString() + " px", "Resources.DisplayMetrics"),
                Reading("Density", dm.densityDpi.toString() + " dpi", "Resources.DisplayMetrics"),
                Reading("Reported refresh rate", reportedHz?.takeIf { it > 0 }?.let { decimal(it.toDouble()) + " Hz" }, "DisplayManager")
            ),
            system = listOf(
                Reading("Manufacturer", Build.MANUFACTURER, "Build"),
                Reading("Device model", Build.MODEL, "Build"),
                Reading("Android", Build.VERSION.RELEASE, "Build.VERSION"),
                Reading("API level", Build.VERSION.SDK_INT.toString(), "Build.VERSION"),
                Reading("Security patch", Build.VERSION.SECURITY_PATCH, "Build.VERSION")
            ),
            sensors = listOf(Reading("Detected sensors", detectedSensors.size.toString(), "SensorManager")) +
                detectedSensors.take(32).map { Reading(it.name, it.vendor, "SensorManager") }
        )
    }
    private fun decimal(value: Double): String = String.format(Locale.US, "%.1f", value)
    private fun bytes(value: Long): String {
        return if (value >= 1_073_741_824L) decimal(value / 1_073_741_824.0) + " GiB"
        else decimal(value / 1_048_576.0) + " MiB"
    }
}
