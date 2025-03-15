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

/** Minimum distance to a [point]. If this is a polygon(s), the distance is 0 if [point] is within
 *  this polygon(s). */
fun ElementGeometry.distance(point: LatLon): Double =
    when (this) {
        is ElementPointGeometry -> {
            center.distanceTo(point)
        }
        is ElementPolylinesGeometry -> {
            polylines.filter { it.isNotEmpty() }.minOf { point.distanceToArcs(it) }
        }
        is ElementPolygonsGeometry -> {
            if (polygons.any { point.isInPolygon(it) }) {
                0.0
            } else {
                polygons.filter { it.isNotEmpty() }.minOf { point.distanceToArcs(it) }
            }
        }
    }

private fun ElementGeometry.asList(): List<List<LatLon>> = when (this) {
    is ElementPointGeometry -> listOf(listOf(center))
    is ElementPolygonsGeometry -> polygons
    is ElementPolylinesGeometry -> polylines
}
