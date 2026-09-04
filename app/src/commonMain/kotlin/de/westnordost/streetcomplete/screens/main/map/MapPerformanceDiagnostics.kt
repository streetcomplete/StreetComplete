package de.westnordost.streetcomplete.screens.main.map

import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.atomic

/** Detailed map timings enabled only by the deterministic performance scenario. */
internal object MapPerformanceDiagnostics {
    var enabled: Boolean = false
    private val currentPhase = atomic<String?>(null)

    fun beginPhase(name: String) {
        currentPhase.value = name
    }

    fun endPhase(name: String) {
        currentPhase.compareAndSet(name, null)
    }

    fun contextualize(message: String): String =
        "$message phase=${currentPhase.value ?: "none"}"

    fun log(message: () -> String) {
        if (enabled) Log.d("MapPinsPerformance", contextualize(message()))
    }

    fun logSource(message: () -> String) {
        if (enabled) Log.d("MapSourcePerformance", contextualize(message()))
    }
}
