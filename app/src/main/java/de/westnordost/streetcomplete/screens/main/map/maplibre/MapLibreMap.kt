package de.westnordost.streetcomplete.screens.main.map.maplibre

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun MapLibreMap.awaitSetStyle(builder: Style.Builder): Style = suspendCoroutine { cont ->
    setStyle(builder) { cont.resume(it) }
}
