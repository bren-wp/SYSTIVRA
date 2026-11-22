// SYSTIVRA by Brendigo
package com.brendigo.systivra.data

/** Atomic session selection: stale SQLite results cannot replace a newer request. */
internal data class SelectedRecordingState(
    val id: Long? = null,
    val samples: List<DeviceSnapshot> = emptyList(),
    val loading: Boolean = false,
    val generation: Long = 0L
) {
    fun select(targetId: Long?): SelectedRecordingState =
        SelectedRecordingState(id = targetId, loading = true, generation = generation + 1)

    fun resolve(requestedId: Long?, requestedGeneration: Long, readings: List<DeviceSnapshot>): SelectedRecordingState =
        if (id == requestedId && generation == requestedGeneration) {
            copy(samples = if (id == null) emptyList() else readings.toList(), loading = false)
        } else this

    fun fail(requestedId: Long?, requestedGeneration: Long): SelectedRecordingState =
        if (id == requestedId && generation == requestedGeneration) {
            copy(samples = emptyList(), loading = false)
        } else this

    fun clear(): SelectedRecordingState = SelectedRecordingState(generation = generation + 1)
}
