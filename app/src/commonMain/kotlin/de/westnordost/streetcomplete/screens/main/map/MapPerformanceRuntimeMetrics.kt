package de.westnordost.streetcomplete.screens.main.map

/** Latest completed runtime garbage-collection pause, when the platform exposes it. */
internal data class MapPerformanceGcPause(
    val epoch: Long,
    val firstPauseNanos: Long,
    val secondPauseNanos: Long?,
)

internal expect fun latestMapPerformanceGcPause(): MapPerformanceGcPause?
