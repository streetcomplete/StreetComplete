package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import kotlin.math.abs

fun ElementPolylinesGeometry.getOrientationAtCenterLineInDegrees(): Float {
    val centerLine = polylines.first().centerLineOfPolyline()
    return centerLine.first.initialBearingTo(centerLine.second).toFloat()
}

/** Returns whether any individual line segment in this ElementPolylinesGeometry is both within
 *  [maxDistance]m of any line segments of [others] and also
 *  and "aligned", meaning the angle difference is at most the given [maxAngle]
 *
 *  Warning: This is computationally very expensive ( for normal ways, O(n³) ), avoid if possible */
fun ElementPolylinesGeometry.isNearAndAligned(
    maxDistance: Double,
    maxAngle: Double,
    others: Iterable<ElementPolylinesGeometry>
): Boolean {
    val bounds = getBounds().enlargedBy(maxDistance)
    return others.any { other ->
        bounds.intersect(other.getBounds())
        && polylines.any { polyline ->
            other.polylines.any { otherPolyline ->
                polyline.isWithinDistanceAndAngleOf(otherPolyline, maxDistance, maxAngle)
            }
        }
    }
}

private fun List<LatLon>.isWithinDistanceAndAngleOf(other: List<LatLon>, maxDistance: Double, maxAngle: Double): Boolean {
    asSequenceOfPairs().forEach { (first, second) ->
        other.asSequenceOfPairs().forEach { (otherFirst, otherSecond) ->
            val bearing = first.initialBearingTo(second)
            val otherBearing = otherFirst.initialBearingTo(otherSecond)
            val bearingDiff = abs(normalizeDegrees(bearing - otherBearing, -180.0))
            // two ways directly opposite each other should count as aligned
            val alignmentDiff = if (bearingDiff > 90) 180 - bearingDiff else bearingDiff
            val distance = first.distanceToArc(otherFirst, otherSecond)
            if (alignmentDiff <= maxAngle && distance <= maxDistance) {
                return true
            }
        }
    }
    return false
}

fun ElementPolylinesGeometry.intersects(other: ElementPolylinesGeometry): Boolean =
    getBounds().intersect(other.getBounds())
    && polylines.any { polyline ->
        other.polylines.any { otherPolyline ->
            polyline.intersectsWith(otherPolyline)
        }
    }
