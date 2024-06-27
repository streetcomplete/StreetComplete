package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.graphics.PointF
import android.graphics.RectF
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Layer
import org.maplibre.geojson.Feature
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun MapLibreMap.awaitSetStyle(builder: Style.Builder): Style = suspendCoroutine { cont ->
    setStyle(builder) { cont.resume(it) }
}

fun MapLibreMap.queryRenderedFeatures(
    coordinates: PointF,
    radius: Float,
    vararg layerIds: String
): List<Feature> {
    val searchArea = RectF(
        coordinates.x - radius,
        coordinates.y - radius,
        coordinates.x + radius,
        coordinates.y + radius
    )
    return queryRenderedFeatures(searchArea, *layerIds)
}
