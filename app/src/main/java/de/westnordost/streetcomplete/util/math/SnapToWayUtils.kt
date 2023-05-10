package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import kotlin.math.min

sealed interface PositionOnWay {
    val position: LatLon
}

/** A vertex of one or several ways */
data class VertexOfWay(
    val wayIds: Set<Long>,
    override val position: LatLon,
    val nodeId: Long,
) : PositionOnWay

/** A point on a segment of a way */
data class PositionOnWaySegment(
    val wayId: Long,
    override val position: LatLon,
    val segment: Pair<LatLon, LatLon>
) : PositionOnWay

/** Returns the point on any of the given ways that is nearest to this point and at most
 *  [maxDistance] meters away from this point or null if there is no such point.
 *
 *  Additionally, if this point is closer than [snapToVertexDistance] near a vertex of a way, that
 *  vertex is chosen instead, even if it is not closest to this point.
 *   */
fun LatLon.getPositionOnWays(
    ways: Collection<Pair<Way, List<LatLon>>>,
    maxDistance: Double,
    snapToVertexDistance: Double = 0.0
): PositionOnWay? {
    if (snapToVertexDistance > 0.0) {
        val nearestVertex = getNearestVertexOfWays(ways, min(maxDistance, snapToVertexDistance))
        if (nearestVertex != null) return nearestVertex
    }

    return getNearestPositionToWays(ways, maxDistance)
}

/** Returns the nearest vertex of any of the ways given in [ways] that is at  most [maxDistance]
 *  away from this point or null if there isn't any */
private fun LatLon.getNearestVertexOfWays(
    ways: Collection<Pair<Way, List<LatLon>>>,
    maxDistance: Double,
): VertexOfWay? {
    var minDistance = Double.MAX_VALUE
    var nearestNodeId: Long? = null
    var nearestPoint: LatLon? = null
    var nearestWayIds = mutableSetOf<Long>()
    for ((way, positions) in ways) {
        for ((i, nodeId) in way.nodeIds.withIndex()) {
            if (nodeId == nearestNodeId) {
                nearestWayIds.add(way.id)
                continue
            }
            val point = positions[i]

            val distance = distanceTo(point)
            if (distance < minDistance && distance <= maxDistance) {
                minDistance = distance
                nearestNodeId = nodeId
                nearestPoint = point
                nearestWayIds = mutableSetOf(way.id)
            }
        }
    }

    if (nearestNodeId != null && nearestPoint != null) {
        return VertexOfWay(nearestWayIds, nearestPoint, nearestNodeId)
    }
    return null
}

/** Returns the nearest point on any of the ways given in [ways] that is at most [maxDistance]
 *  away from this point or null if there isn't any */
private fun LatLon.getNearestPositionToWays(
    ways: Collection<Pair<Way, List<LatLon>>>,
    maxDistance: Double
): PositionOnWaySegment? {
    var minDistance = Double.MAX_VALUE
    var nearestWay: Way? = null
    var nearestSegment: Pair<LatLon, LatLon>? = null
    for ((way, positions) in ways) {
        for (segment in positions.asSequenceOfPairs()) {
            val distance = distanceToArc(segment.first, segment.second)
            if (distance < minDistance && distance <= maxDistance) {
                minDistance = distance
                nearestWay = way
                nearestSegment = segment
            }
        }
    }
    if (nearestWay != null && nearestSegment != null) {
        val nearestPoint = nearestPointOnArc(nearestSegment.first, nearestSegment.second)
        return PositionOnWaySegment(nearestWay.id, nearestPoint, nearestSegment)
    }
    return null
}

private val ElementGeometry.wayLatLons: List<LatLon> get() = when (this) {
    // single is safe because a way cannot have multiple polygons / polylines
    is ElementPolygonsGeometry -> polygons.single()
    is ElementPolylinesGeometry -> polylines.single()
    else -> throw IllegalStateException()
}
