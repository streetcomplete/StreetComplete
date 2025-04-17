package de.westnordost.streetcomplete.screens.main.map.maplibre

import android.graphics.PointF
import android.graphics.RectF
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
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
    // first try without radius. If nothing found, only then expand search area
    var result = queryRenderedFeatures(coordinates, *layerIds)
    if (result.isNotEmpty()) return result
    // then, try with small radius....
    result = queryRenderedFeatures(coordinates.expandBy(radius / 2), *layerIds)
    if (result.isNotEmpty()) return result

    return queryRenderedFeatures(coordinates.expandBy(radius), *layerIds)

    // this is kind of a workaround. We'd need is this function implemented in MapLibre proper. See
    // https://github.com/maplibre/maplibre-native/issues/2781
}

private fun PointF.expandBy(a: Float) = RectF(x - a, y - a, x + a, y + a)
