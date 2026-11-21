package com.brendigo.systivra.ui

import com.brendigo.systivra.data.DeviceSnapshot

/**
 * Each Storage Access Framework request exports exactly the samples visible
 * when the user pressed Export. The picker can stay open while recordings or
 * the selected session change. Missing pending state after process recreation
 * must never silently export a different session.
 */
internal class PendingReportExport {
    private var pending: List<DeviceSnapshot>? = null

    fun prepare(samples: List<DeviceSnapshot>) {
        pending = samples.map { snap ->
            snap.copy(
                cpu = snap.cpu.toList(),
                gpu = snap.gpu.toList(),
                ram = snap.ram.toList(),
                storage = snap.storage.toList(),
                battery = snap.battery.toList(),
                thermal = snap.thermal.toList(),
                display = snap.display.toList(),
                system = snap.system.toList(),
                sensors = snap.sensors.toList()
            )
        }
    }

    fun consume(): List<DeviceSnapshot>? =
        pending.also { pending = null }
}
