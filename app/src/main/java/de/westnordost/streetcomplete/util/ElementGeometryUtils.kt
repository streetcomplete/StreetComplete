package de.westnordost.streetcomplete.util

import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.ktx.forEachLine
import kotlin.math.abs

fun ElementPolylinesGeometry.getOrientationAtCenterLineInDegrees(): Float {
    val centerLine = polylines.first().centerLineOfPolyline()
    return centerLine.first.initialBearingTo(centerLine.second).toFloat()
}

/** Returns whether any individual line segment in this ElementPolylinesGeometry is both within
 *  [maxDistance]m of any line segments of [others] and also
 *  and "aligned", meaning
 *
 *  Warning: This is computationally very expensive ( for normal ways, O(n³) ), avoid if possible */
fun ElementPolylinesGeometry.isNearAndAligned(
    maxDistance: Double,
    maxAngle: Double,
    others: Iterable<ElementPolylinesGeometry>
): Boolean {
    val bounds = getBounds().enlargedBy(maxDistance)
    return others.any { other ->
        bounds.intersect(other.getBounds()) &&
        polylines.any { polyline ->
            other.polylines.any { otherPolyline ->
                polyline.isWithinDistanceAndAngleOf(otherPolyline, maxDistance, maxAngle)
            }
        }
    }
}

private fun List<LatLon>.isWithinDistanceAndAngleOf(other: List<LatLon>, maxDistance: Double, maxAngle: Double): Boolean {
    forEachLine { first, second ->
        other.forEachLine { otherFirst, otherSecond ->
            val bearing = first.initialBearingTo(second)
            val otherBearing = otherFirst.initialBearingTo(otherSecond)
            val bearingDiff = abs((bearing - otherBearing).normalizeDegrees(-180.0))
            val distance = first.distanceToArc(otherFirst, otherSecond)
            if (bearingDiff <= maxAngle && distance <= maxDistance)
                return true
        }
    }
    return false
}
