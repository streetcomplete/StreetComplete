package de.westnordost.streetcomplete.util.math

import de.westnordost.streetcomplete.data.osm.edits.create.InsertIntoWayAt
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.ktx.asSequenceOfPairs
import kotlin.math.abs
import kotlin.math.min

@kotlinx.serialization.Serializable
sealed interface PositionOnWay {
    val position: LatLon
}

/** A vertex of one or several ways */
@kotlinx.serialization.Serializable
data class VertexOfWay(
    val wayIds: Set<Long>,
    override val position: LatLon,
    val nodeId: Long,
) : PositionOnWay

/** A point on a segment of a way */
@kotlinx.serialization.Serializable
data class PositionOnWaySegment(
    val wayId: Long,
    override val position: LatLon,
    val segment: Pair<LatLon, LatLon>
) : PositionOnWay

/** A point on one or ways on top of each other (typically but not necessarily sharing segments) */
@kotlinx.serialization.Serializable
data class PositionOnWaysSegment(
    override val position: LatLon,
    val insertIntoWaysAt: List<InsertIntoWayAt>
): PositionOnWay {
    init {
        require(insertIntoWaysAt.map { it.wayId }.toHashSet().size == insertIntoWaysAt.size) { "can't insert in the same way more than once" }
    }
}

/** A crossing point of two ways that are not connected at the crossing */
@kotlinx.serialization.Serializable
data class PositionOnCrossingWaySegments(
    override val position: LatLon,
    val insertIntoWaysAt: List<InsertIntoWayAt>
): PositionOnWay {
    init {
        require(insertIntoWaysAt.size == 2 && insertIntoWaysAt.first().wayId != insertIntoWaysAt.last().wayId) { "must be 2 different ways" }
    }
}

/** Returns the point on any of the given ways that is nearest to this point and at most
 *  [maxDistance] meters away from this point or null if there is no such point.
 *
 *  Additionally, if this point is closer than [snapToVertexDistance] near a vertex of a way, that
 *  vertex is chosen instead, even if it is not closest to this point.
 */
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
    var nearestWayIds = mutableListOf<Long>()
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
                nearestWayIds = mutableListOf(way.id)
            }
        }
    }

    if (nearestNodeId != null && nearestPoint != null) {
        return VertexOfWay(nearestWayIds.toHashSet(), nearestPoint, nearestNodeId)
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

/** same as getPositionOnWays, but using PositionOnWaysSegment instead of PositionOnWaySegment
 * (i.e. allowing multiple ways), and before that checks for crossing ways */
fun LatLon.getPositionOnWaysForInsertNodeFragment(
    ways: Collection<Pair<Way, List<LatLon>>>,
    maxDistance: Double,
    snapToVertexDistance: Double = 0.0
): PositionOnWay? {
    if (snapToVertexDistance > 0.0) {
        val nearestVertex = getNearestVertexOfWays(ways, min(maxDistance, snapToVertexDistance))
        if (nearestVertex != null) return nearestVertex
    }
    // distanceToArcs is called often, and if there is no nearby position found: called (at least)
    // twice for every segment. This mini-cache mitigates calling it twice (also possibly more often
    // for two ways having the same segments, but that's likely negligible)
    val distanceToArcsCache = HashMap<Pair<LatLon, LatLon>, Double>(ways.size * 3)

    return getNearestCrossingOfTwoWays(ways, maxDistance, distanceToArcsCache)
        ?: getNearestPositionToWaysOnMultipleWays(ways, maxDistance, distanceToArcsCache)
}

private fun LatLon.getNearestPositionToWaysOnMultipleWays(
    ways: Collection<Pair<Way, List<LatLon>>>,
    maxDistance: Double,
    distanceToArcsCache: MutableMap<Pair<LatLon, LatLon>, Double>,
): PositionOnWaysSegment? {
    var minDistance = Double.MAX_VALUE
    var nearestWays = mutableListOf<InsertIntoWayAt>()
    for ((way, positions) in ways) {
        for (segment in positions.asSequenceOfPairs()) {
            // todo: do level / layer checks (maybe)
            val distance = distanceToArcsCache.getOrPut(segment) { distanceToArc(segment.first, segment.second) }
            if (distance > maxDistance) continue
            if (abs(distance - minDistance) < 5e-4) {
                // we could check whether segments are actually parallel with small distance only,
                // but since we check for crossing ways right before this, there are only very few
                // ways we could filter out here (very near and almost parallel) -> maybe do something if an actual situation demands it
                if (nearestWays.any { it.wayId == way.id })
                    continue // don't add another segment of the same way, for now don't care if the second one would be closer
                nearestWays.add(InsertIntoWayAt(way.id, segment.first, segment.second))
            } else if (distance < minDistance) {
                minDistance = distance
                nearestWays = mutableListOf(InsertIntoWayAt(way.id, segment.first, segment.second))
            }
        }
    }
    if (nearestWays.isNotEmpty()) {
        val nearestPoint = nearestPointOnArc(nearestWays.first().pos1, nearestWays.first().pos2)
        return PositionOnWaysSegment(nearestPoint, nearestWays)
    }
    return null
}

fun LatLon.getNearestCrossingOfTwoWays(
    ways: Collection<Pair<Way, List<LatLon>>>,
    maxDistance: Double,
    distanceToArcsCache: MutableMap<Pair<LatLon, LatLon>, Double>,
): PositionOnCrossingWaySegments? {
    val nearbyWaySegments = mutableListOf<Pair<Way, Pair<LatLon, LatLon>>>()
    for ((way, positions) in ways) {
        for (segment in positions.asSequenceOfPairs()) {
            val distance = distanceToArcsCache.getOrPut(segment) { distanceToArc(segment.first, segment.second) }
            if (distance <= maxDistance) {
                nearbyWaySegments.add(way to segment)
            }
        }
    }
    // check whether any tow of those cross (hmm, this could be many checks and also slow -> log)
    var minDistance = Double.MAX_VALUE
    var bestPosition: PositionOnCrossingWaySegments? = null
    nearbyWaySegments.forEachIndexed { i, (way, segment) ->
        // check whether it crosses any way with higher index
        if (i == nearbyWaySegments.lastIndex) return@forEachIndexed
        // this double loop has the potential to explode, but actually it's rarely more than 5 ways,
        // and thus much faster then finding distance to segments anyway
        for (j in i+1 until nearbyWaySegments.size) {
            val secondSegment = nearbyWaySegments[j].second
            val secondWay = nearbyWaySegments[j].first
            if (way.id == secondWay.id) continue // ignore ways crossing themselves
            // todo: do level / layer checks (maybe)
            val intersection = intersectionOf(segment.first, segment.second, secondSegment.first, secondSegment.second) ?: continue
            val distance = distanceTo(intersection)
            if (distance < minDistance && distance <= maxDistance) {
                minDistance = distance
                val insert1 = InsertIntoWayAt(way.id, segment.first, segment.second)
                val insert2 = InsertIntoWayAt(secondWay.id, secondSegment.first, secondSegment.second)
                bestPosition = PositionOnCrossingWaySegments(intersection, listOf(insert1, insert2))
            }
        }
    }
    return bestPosition
}

private val ElementGeometry.wayLatLons: List<LatLon> get() = when (this) {
    // single is safe because a way cannot have multiple polygons / polylines
    is ElementPolygonsGeometry -> polygons.single()
    is ElementPolylinesGeometry -> polylines.single()
    else -> throw IllegalStateException()
}
