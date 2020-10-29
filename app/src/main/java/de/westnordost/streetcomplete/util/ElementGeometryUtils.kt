package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry

fun ElementPolylinesGeometry.getOrientationAtCenterLineInDegrees(): Float {
    val centerLine = polylines.first().centerLineOfPolyline()
    return centerLine.first.initialBearingTo(centerLine.second).toFloat()
}

/** Returns whether this ElementPolylinesGeometry is near a set of other ElementPolylinesGeometries
 *
 *  Warning: This is computationally very expensive ( for normal ways, O(n³) ), avoid if possible */
fun ElementPolylinesGeometry.isNear(
    maxDistance: Double,
    others: Iterable<ElementPolylinesGeometry>
): Boolean {
    val bounds = getBounds().enlargedBy(maxDistance)
    return others.any { other ->
        bounds.intersect(other.getBounds()) &&
                polylines.any { polyline ->
                    other.polylines.any { otherPolyline ->
                        polyline.distanceTo(otherPolyline) <= maxDistance
                    }
                }
    }
}
