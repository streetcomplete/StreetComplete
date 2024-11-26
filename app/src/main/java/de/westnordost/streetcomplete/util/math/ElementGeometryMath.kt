package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun ElementPolylinesGeometry.getOrientationAtCenterLineInDegrees(): Float {
    val centerLine = polylines.first().centerLineOfPolyline()
    return centerLine.first.initialBearingTo(centerLine.second).toFloat()
}

fun ElementGeometry.intersects(other: ElementGeometry): Boolean {
    // not interested in point geometry here
    if (this is ElementPointGeometry || other is ElementPointGeometry) return false

    if (!getBounds().intersect(other.getBounds())) return false

    return asList().any { polyline ->
        other.asList().any { otherPolyline ->
            polyline.intersectsWith(otherPolyline)
        }
    }
}

private fun ElementGeometry.asList(): List<List<LatLon>> = when (this) {
    is ElementPointGeometry -> listOf(listOf(center))
    is ElementPolygonsGeometry -> polygons
    is ElementPolylinesGeometry -> polylines
}
