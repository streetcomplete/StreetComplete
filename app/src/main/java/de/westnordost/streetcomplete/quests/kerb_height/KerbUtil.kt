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
    ways.asSequence()
        .filter { footwaysFilter.matches(it) }
        .flatMap { it.nodeIds }
        .mapNotNullTo(footwayNodes) { getNode(it) }

    val kerbBarrierNodeIds = mutableSetOf<Long>()
    ways.asSequence()
        .filter { it.tags?.get("barrier") == "kerb" }
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

/** Find all node ids of end nodes of crossings that are (very probably) kerbs within the given
 *  collection of [ways] */
private fun findCrossingKerbEndNodeIds(ways: Collection<Way>): Set<Long> {
    /* using asSequence in this function so to not copy potentially huge amounts (f.e. almost all
       nodes of all ways in the data set) of data into temporary lists */

    val footways = ways.filter { footwaysFilter.matches(it) }

    val crossingEndNodeIds = footways.asSequence()
        .filter { it.tags?.get("footway") == "crossing" }
        .flatMap { it.nodeIds.firstAndLast() }

    val connectionsById = mutableMapOf<Long, Int>()
    for (id in crossingEndNodeIds) {
        val count = connectionsById[id] ?: 0
        connectionsById[id] = count + 1
    }
    // skip nodes that have not exactly ONE connection to a crossing
    connectionsById.entries.removeAll { it.value != 1 }
    if (connectionsById.isEmpty()) return emptySet()

    val sidewalkEndNodeIds = footways.asSequence()
        .filter { it.tags?.get("footway") == "sidewalk" }
        .flatMap { it.nodeIds.firstAndLast() }

    for (id in sidewalkEndNodeIds) {
        val count = connectionsById[id] ?: continue
        connectionsById[id] = count + 1
    }
    // skip nodes that have not exactly ONE connection to a sidewalk (1 to crossing + 1 to sidewalk = 2)
    connectionsById.entries.removeAll { it.value != 2 }
    if (connectionsById.isEmpty()) return emptySet()

    // skip nodes that share an end node with a way where it is not clear if it is a sidewalk, crossing or something else
    ways.asSequence()
        .filter { it.tags?.get("footway") != "sidewalk" && it.tags?.get("footway") != "crossing" }
        .flatMap { it.nodeIds.firstAndLast() }
        .forEach { connectionsById.remove(it) }
    if (connectionsById.isEmpty()) return emptySet()

    // skip nodes that share an end node with any node of a way that is not an end node
    ways.asSequence()
        .flatMap { it.nodeIds.subList(1, it.nodeIds.size - 1) }
        .forEach { connectionsById.remove(it) }
    if (connectionsById.isEmpty()) return emptySet()

    return connectionsById.keys
}
