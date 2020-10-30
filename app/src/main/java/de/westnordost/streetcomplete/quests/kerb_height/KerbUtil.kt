package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Node
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.ktx.firstAndLast

private val footwaysFilter by lazy {"""
    ways with (
        highway ~ footway|path
        or highway = cycleway and foot ~ yes|designated
      )
      and access !~ private|no and foot !~ no
""".toElementFilterExpression() }

fun MapData.findAllKerbNodes(): Iterable<Node> {
    val footways = ways.filter { footwaysFilter.matches(it) }

    val crossingEndNodes = footways
        .filter { it.tags?.get("footway") == "crossing" }
        .flatMap { it.nodeIds.firstAndLast() }
        .mapNotNull { getNode(it) }

    val sidewalkEndNodes = footways
        .filter { it.tags?.get("footway") == "sidewalk" }
        .flatMap { it.nodeIds.firstAndLast() }
        .mapNotNull { getNode(it) }

    val crossingEndNodesConnectionCountByIds = mutableMapOf<Long, Int>()
    for (nd in crossingEndNodes) {
        crossingEndNodesConnectionCountByIds[nd.id] = 1
    }
    for (nd in sidewalkEndNodes) {
        val prevCount = crossingEndNodesConnectionCountByIds[nd.id] ?: 0
        if (prevCount > 0) crossingEndNodesConnectionCountByIds[nd.id] = prevCount + 1
    }

    val footwayNodes = mutableSetOf<Node>()
    footways
        .flatMap { it.nodeIds }
        .mapNotNullTo(footwayNodes) { getNode(it) }

    val kerbBarrierNodeIds = mutableSetOf<Long>()
    ways.filter { it.tags?.get("barrier") == "kerb" }
        .flatMapTo(kerbBarrierNodeIds) { it.nodeIds }

    // Kerbs can be defined in three ways (see https://github.com/westnordost/StreetComplete/issues/1305#issuecomment-688333976):
    return footwayNodes.filter {
        // 1. either as the intersection of a way tagged with barrier = kerb that crosses a footway
        it.tags?.get("barrier") == "kerb" ||
        // 2. or as a node tagged with barrier = kerb on a footway
        kerbBarrierNodeIds.contains(it.id) ||
        // 3. or implicitly as the shared node between a footway tagged with footway = crossing and
        //    another tagged with footway = sidewalk that is the continuation of the way and is not
        //    and intersection (thus, has exactly two connections: to the sidewalk and to the crossing)
        crossingEndNodesConnectionCountByIds[it.id] == 2
    }
}