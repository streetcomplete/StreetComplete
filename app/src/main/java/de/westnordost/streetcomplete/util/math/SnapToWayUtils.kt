package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlin.math.min


sealed interface PointOnWay

/** A vertex of a way */
data class VertexOfWay(
    val wayId: Long,
    val point: LatLon
) : PointOnWay

/** A point on a segment of a way */
data class PointOnWaySegment(
    val wayId: Long,
    val point: LatLon,
    val segment: Pair<LatLon, LatLon>
): PointOnWay


/** Returns the point on any of the given ways that is nearest to this point and at most
 *  [maxDistance] meters away from this point or null if there is no such point.
 *
 *  Additionally, if this point is closer than [snapToVertexDistance] near a vertex of a way, that
 *  vertex is chosen instead, even if it is not closest to this point.
 *   */
fun LatLon.getPointOnWays(
    geometriesByWayId: Map<Long, List<LatLon>>,
    maxDistance: Double,
    snapToVertexDistance: Double = 0.0
): PointOnWay? {
    if (snapToVertexDistance > 0.0) {
        val (wayId, nearestVertex) = geometriesByWayId
            .mapValues { getNearestVertex(it.value) }
            .minBy { distanceTo(it.value) }

        if (distanceTo(nearestVertex) <= min(maxDistance, snapToVertexDistance)) {
            return VertexOfWay(wayId, nearestVertex)
        }
    }

    val (wayId2, nearestArc) = geometriesByWayId
        .mapValues { nearestArcOf(it.value) }
        .minBy { distanceToArc(it.value.first, it.value.second) }

    if (distanceToArc(nearestArc.first, nearestArc.second) <= maxDistance) {
        val nearestPoint = nearestPointOnArc(nearestArc.first, nearestArc.second)
        return PointOnWaySegment(wayId2, nearestPoint, nearestArc)
    }

    return null
}

private fun LatLon.getNearestVertex(polyline: List<LatLon>): LatLon =
    polyline.minBy { distanceTo(it) }
