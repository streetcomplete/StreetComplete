package de.westnordost.streetcomplete.screens.main.map

import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

@OptIn(ExperimentalStdlibApi::class, NativeRuntimeApi::class)
internal actual fun latestMapPerformanceGcPause(): MapPerformanceGcPause? {
    val info = GC.lastGCInfo ?: return null
    return MapPerformanceGcPause(
        epoch = info.epoch,
        firstPauseNanos = info.firstPauseEndTimeNs - info.firstPauseStartTimeNs,
        secondPauseNanos = info.secondPauseEndTimeNs?.let { end ->
            info.secondPauseStartTimeNs?.let { start -> end - start }
        },
    )
}
