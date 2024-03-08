package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapData
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.math.isRightOf

data class WaysCrossing(val node: Node, var barrierWays: List<Way>, var movingWays: List<Way>)

/** Return all crossings with nodes that are at crossing points between movingWays and barrierWays.
 *  Returned data includes both nodes at actual crossing and ways creating it
 *  to allow additional filtering
 *
 *  In this context, two ways cross if
 *  1. they intersect at a shared node
 *  2. the node is not the end node of any of the two ways
 *  3. the ways actually cross each other, not just touch each other, i.e. the moving way
 *     intersects, then continues on the other side of a barrier way
 */
fun findNodesAtCrossingsOf(barrierWays: Sequence<Way>, movingWays: Sequence<Way>, mapData: MapData): Iterable<WaysCrossing> {
    val barriersByNodeId = barrierWays.groupByNodeIds()
    // filter out nodes of roads that are the end of a barrier not continuing further
    barriersByNodeId.removeEndNodes()

    val waysByNodeId = movingWays.groupByNodeIds()

    /* filter out nodes of footways that are the end of a graph, e.g.
     * in case of ways representing footways following nodes would be skipped
     * https://www.openstreetmap.org/node/9124092987 (intersecting with barriers)
     * https://www.openstreetmap.org/node/1449039062 (with roads)
     * https://www.openstreetmap.org/node/56606744 (with roads) */
    waysByNodeId.removeEndNodes()

    // filter out all nodes that are not shared nodes of both a road and a footway
    barriersByNodeId.keys.retainAll(waysByNodeId.keys)
    waysByNodeId.keys.retainAll(barriersByNodeId.keys)

    /* finally, filter out all shared nodes where the footway(s) do not actually cross the barrier(s).
     * There are two situations which both need to be handled:
     *
     * 1. The shared node is contained in a both ways and it is not an end
     *    node of any of the involved ways, e.g.
     *    https://www.openstreetmap.org/node/2225781269 (intersecting footways with barriers)
     *    https://www.openstreetmap.org/node/8418974983 (intersecting footways with roads)
     *
     * 2. The barrier way or the footway way or both actually end on the shared node but are
     *    connected to another which continues the way after
     *    https://www.openstreetmap.org/node/2458449002 (transition of footway to steps - intersecting with barrier)
     *    https://www.openstreetmap.org/node/1641565064 (intersecting footways with roads)
     *
     * So, for the algorithm, it should be irrelevant to which way(s) the segments around the
     * shared node belong, what count are the positions / angles.
     */
    waysByNodeId.entries.retainAll { (nodeId, ways) ->

        val barriers = barriersByNodeId.getValue(nodeId)
        val neighbouringBarrierPositions = barriers
            .flatMap { it.getNodeIdsNeighbouringNodeId(nodeId) }
            .mapNotNull { mapData.getNode(it)?.position }

        val neighbouringWayPositions = ways
            .flatMap { it.getNodeIdsNeighbouringNodeId(nodeId) }
            .mapNotNull { mapData.getNode(it)?.position }

        /* So, surrounding the shared node X, in the simple case, we have
         *
         * 1. position A, B neighbouring the shared node position which are part of a barrier way
         * 2. position P, Q neighbouring the shared node position which are part of the moving way
         *
         * The way crosses the barrier if P is on one side of the polyline spanned by A,X,B and
         * Q is on the other side.
         *
         * How can a moving way that has a shared node with a barrier not cross the latter?
         * Multiple separate paths can reach barrier in one shared node - all from one side
         * Imagine the road at https://www.openstreetmap.org/node/258003112 would continue to
         * the south here, then, still none of those footways would actually cross the street
         *  - only if it continued straight to the north, for example.
         *
         * The example brings us to the less simple case: What if several barriers share
         * a node at a crossing-candidate position?
         * Also, what if there are more than one footways involved?
         *
         * We look for if there is ANY crossing, so all polylines involved are checked:
         * For all barriers going through point X, it is checked if not all passing ways that
         * go through X are on the same side of the barrier-polyline.
         * */
        val nodePos = mapData.getNode(nodeId)?.position
        return@retainAll nodePos != null &&
            neighbouringWayPositions.anyCrossesAnyOf(neighbouringBarrierPositions, nodePos)
    }

    return waysByNodeId.map { (nodeId, ways) ->
        WaysCrossing(
            mapData.getNode(nodeId)!!,
            barriersByNodeId.getValue(nodeId),
            ways
        )
    }
}

/** get the node id(s) neighbouring to the given node id */
private fun Way.getNodeIdsNeighbouringNodeId(nodeId: Long): List<Long> {
    val idx = nodeIds.indexOf(nodeId)
    if (idx == -1) return emptyList()
    val prevNodeId = if (idx > 0) nodeIds[idx - 1] else null
    val nextNodeId = if (idx < nodeIds.size - 1) nodeIds[idx + 1] else null
    return listOfNotNull(prevNodeId, nextNodeId)
}

private fun MutableMap<Long, MutableList<Way>>.removeEndNodes() {
    entries.removeAll { (nodeId, ways) ->
        ways.size == 1 && (nodeId == ways[0].nodeIds.first() || nodeId == ways[0].nodeIds.last())
    }
}

/** groups the sequence of ways to a map of node id -> list of ways */
private fun Sequence<Way>.groupByNodeIds(): MutableMap<Long, MutableList<Way>> {
    val result = mutableMapOf<Long, MutableList<Way>>()
    forEach { way ->
        way.nodeIds.forEach { nodeId ->
            result.getOrPut(nodeId, { mutableListOf() }).add(way)
        }
    }
    return result
}

/** Returns whether any of the lines spanned by any of the points in this list through the
 *  vertex point cross any of the lines spanned by any of the points through the vertex point
 *  from the other list */
private fun List<LatLon>.anyCrossesAnyOf(other: List<LatLon>, vertex: LatLon): Boolean =
    (1 until size).any { i -> other.anyAreOnDifferentSidesOf(this[0], vertex, this[i]) }

/** Returns whether any of the points in this list are on different sides of the line spanned
 *  by p0 and p1 and the line spanned by p1 and p2 */
private fun List<LatLon>.anyAreOnDifferentSidesOf(p0: LatLon, p1: LatLon, p2: LatLon): Boolean =
    map { it.isRightOf(p0, p1, p2) }.toSet().size > 1
