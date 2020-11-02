package de.westnordost.streetcomplete.util

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.ktx.forEachLine
import kotlin.math.abs

fun ElementPolylinesGeometry.getOrientationAtCenterLineInDegrees(): Float {
    val centerLine = polylines.first().centerLineOfPolyline()
    return centerLine.first.initialBearingTo(centerLine.second).toFloat()
}

/** Returns whether this ElementPolylinesGeometry is near a set of other ElementPolylinesGeometries
 *
 *  Warning: This is computationally very expensive ( for normal ways, O(n³) ), avoid if possible */
fun ElementPolylinesGeometry.isNearAligned(
    maxDistance: Double,
    others: Iterable<ElementPolylinesGeometry>
): Boolean {
    val bounds = getBounds().enlargedBy(maxDistance)
    return others.any { other ->
        bounds.intersect(other.getBounds()) &&
        polylines.any { polyline ->
            other.polylines.any { otherPolyline ->
                polyline.isWithinDistanceAndAngleOf(otherPolyline, 15.0, 45.0)
            }
        }
    }
}

private fun List<LatLon>.isWithinDistanceAndAngleOf(other: List<LatLon>, maxDistance: Double, angle: Double): Boolean {
    forEachLine { first, second ->
        other.forEachLine { otherFirst, otherSecond ->
            val bearing = first.initialBearingTo(second)
            val otherBearing = otherFirst.initialBearingTo(otherSecond)
            val bearingDiff = abs((bearing - otherBearing).normalizeDegrees(-180.0))
            if (bearingDiff <= angle && first.distanceToArc(otherFirst, otherSecond) <= maxDistance)
                return true
        }
    }
    return false
}
