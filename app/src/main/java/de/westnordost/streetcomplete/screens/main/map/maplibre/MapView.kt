package de.westnordost.streetcomplete.screens.main.map.maplibre

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun MapView.awaitGetMap(): MapLibreMap = suspendCoroutine { cont ->
    getMapAsync { cont.resume(it) }
}
