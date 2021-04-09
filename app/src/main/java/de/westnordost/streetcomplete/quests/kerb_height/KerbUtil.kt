package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.ALL_PATHS
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.ktx.firstAndLast

private val footwaysFilter by lazy {"""
    ways with (
        highway ~ footway|path
        or highway = cycleway and foot ~ yes|designated
      )
      and area != yes
      and access !~ private|no and foot !~ no
""".toElementFilterExpression() }

private val waysFilter by lazy {"""
    ways with highway ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")} or construction ~ ${(ALL_ROADS + ALL_PATHS).joinToString("|")}
""".toElementFilterExpression() }

fun MapData.findAllKerbNodes(): Iterable<Node> {
    val footwayNodes = mutableSetOf<Node>()
    ways.filter { footwaysFilter.matches(it) }
        .flatMap { it.nodeIds }
        .mapNotNullTo(footwayNodes) { getNode(it) }

    val kerbBarrierNodeIds = mutableSetOf<Long>()
    ways.filter { it.tags?.get("barrier") == "kerb" }
        .flatMapTo(kerbBarrierNodeIds) { it.nodeIds }

    val anyWays = ways.filter { waysFilter.matches(it) }
    val crossingEndNodeIds = findCrossingKerbEndNodeIds(anyWays)

    // Kerbs can be defined in three ways (see https://github.com/westnordost/StreetComplete/issues/1305#issuecomment-688333976):
    return footwayNodes.filter {
        // 1. either as a node tagged with barrier = kerb on a footway
        it.tags?.get("barrier") == "kerb" ||
        // 2. or as the shared node at which a way tagged with barrier = kerb crosses a footway
        it.id in kerbBarrierNodeIds ||
        // 3. or implicitly as the shared node between a footway tagged with footway = crossing and
        //    another tagged with footway = sidewalk that is the continuation of the way and is not
        //    and intersection (thus, has exactly two connections: to the sidewalk and to the crossing)
        it.id in crossingEndNodeIds
    }
}

enum class Crossing {
    MORE_THAN_ONE_CROSSING_CONNECTION,
    NO_CROSSING_CONNECTION,
    ONE_CROSSING_CONNECTION,
    ONE_CROSSING_AND_ONE_SIDEWALK_CONNECTION,
    ONE_CROSSING_AND_MORE_THAN_ONE_SIDEWALK_CONNECTION
}

/** Find all node ids of end nodes of crossings that are (very probably) kerbs within the given
 *  collection of [ways] */
private fun findCrossingKerbEndNodeIds(ways: Collection<Way>): Set<Long> {
    val footways = ways.filter { footwaysFilter.matches(it) }
    val crossingEndNodesConnectionCountByIds = mutableMapOf<Long, Crossing>()

    val crossingEndNodeIds = footways
        .filter { it.tags?.get("footway") == "crossing" }
        .flatMap { it.nodeIds.firstAndLast() }

    for (id in crossingEndNodeIds) {
        val prevState = crossingEndNodesConnectionCountByIds[id] ?: Crossing.NO_CROSSING_CONNECTION
        if (prevState != Crossing.NO_CROSSING_CONNECTION) {
            // connected already, so invalid - marking rather removing it from map
            // as it may be connected to three or more crossings
            crossingEndNodesConnectionCountByIds[id] = Crossing.MORE_THAN_ONE_CROSSING_CONNECTION
        } else {
            crossingEndNodesConnectionCountByIds[id] = Crossing.ONE_CROSSING_CONNECTION
        }
    }

    // skip nodes that share an end node with a way where it is not clear if it is a sidewalk, crossing or something else
    val unknownEndNodeIds = ways
        .filter { it.tags?.get("footway") != "sidewalk" && it.tags?.get("footway") != "crossing" }
        .flatMap { it.nodeIds.firstAndLast() }
    crossingEndNodesConnectionCountByIds.keys.removeAll(unknownEndNodeIds)

    // skip nodes that share an end node with any node of a way that is not an end node
    val waysMiddleNodeIds = ways.flatMap { it.nodeIds.subList(1, it.nodeIds.size - 1) }
    crossingEndNodesConnectionCountByIds.keys.removeAll(waysMiddleNodeIds)

    // count connections of the remaining crossing end node ids with end nodes of sidewalks
    val sidewalkEndNodeIds = footways
        .filter { it.tags?.get("footway") == "sidewalk" }
        .flatMap { it.nodeIds.firstAndLast() }

    for (id in sidewalkEndNodeIds) {
        val prevState = crossingEndNodesConnectionCountByIds[id] ?: Crossing.NO_CROSSING_CONNECTION
        if (prevState == Crossing.ONE_CROSSING_AND_ONE_SIDEWALK_CONNECTION) {
            crossingEndNodesConnectionCountByIds[id] = Crossing.ONE_CROSSING_AND_MORE_THAN_ONE_SIDEWALK_CONNECTION
        } else if (prevState == Crossing.ONE_CROSSING_CONNECTION) {
            crossingEndNodesConnectionCountByIds[id] = Crossing.ONE_CROSSING_AND_ONE_SIDEWALK_CONNECTION
        }
    }

    // if there are exactly two connections (one to crossing way, one to sidewalk way), this'll be
    // a node where there is a kerb
    return crossingEndNodesConnectionCountByIds.filter { it.value == Crossing.ONE_CROSSING_AND_ONE_SIDEWALK_CONNECTION }.keys
}
