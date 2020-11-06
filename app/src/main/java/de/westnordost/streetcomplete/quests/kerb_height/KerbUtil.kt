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

    val unknownEndNodes = footways
        .filter { it.tags?.get("footway") != "sidewalk" && it.tags?.get("footway") != "crossing" }
        .flatMap { it.nodeIds.firstAndLast() }
        .mapNotNull { getNode(it) }


    val crossingEndNodesConnectionCountByIds = mutableMapOf<Long, Int>()
    for (nd in crossingEndNodes) {
        crossingEndNodesConnectionCountByIds[nd.id] = 1
    }
    // now all nodes with at least one footway=crossing terminating
    // on the node will have value 1 in crossingEndNodesConnectionCountByIds

    for (nd in sidewalkEndNodes) {
        val prevCount = crossingEndNodesConnectionCountByIds[nd.id] ?: 0
        if (prevCount > 0) crossingEndNodesConnectionCountByIds[nd.id] = prevCount + 1
    }
    // now all nodes with exactly one footway=sidewalk with an end on the node
    // that had at least one footway=crossing with an end on the node
    // will have value 2 in crossingEndNodesConnectionCountByIds

    // nodes with at least one footway=sidewalk ending at the given node
    // and more than one footway=sidewalk ending on the node
    // have value 3 or greater

    /*
    for (nd in unknownEndNodes) {
        val prevCount = crossingEndNodesConnectionCountByIds[nd.id] ?: 0
        if (prevCount > 0) crossingEndNodesConnectionCountByIds[nd.id] = -1
    }
    */

    // now all nodes with ending of a way (queried by footwaysFilter)
    // that had neither foootway=crossing or footway=sidewalk
    // will get value -1
    // this is necessary as highway=crossing way may end not on a kerb
    // but on sidewalk
    //
    // 3 ways, all ending on node x that should not be selected
    //
    // 1-----x------2
    //       |
    //       |
    //       |
    //       3
    // way 1: highway=footway footway=sidewalk
    // way 2: highway=footway
    // way 3: highway=footway footway=crossing
    //
    // such incomplete tagging is not wrong, and StreetComplete needs to
    // handle it

    val footwayNodes = mutableSetOf<Node>()
    footways
        .flatMap { it.nodeIds }
        .mapNotNullTo(footwayNodes) { getNode(it) }

    val kerbBarrierNodeIds = mutableSetOf<Long>()
    ways.filter { it.tags?.get("barrier") == "kerb" }
        .flatMapTo(kerbBarrierNodeIds) { it.nodeIds }

    // Kerbs can be defined in three ways (see https://github.com/westnordost/StreetComplete/issues/1305#issuecomment-688333976):
    return footwayNodes.filter {
        // 1. either as a node tagged with barrier = kerb on a footway
        it.tags?.get("barrier") == "kerb" ||
        // 2. or as the shared node at which a way tagged with barrier = kerb crosses a footway
        kerbBarrierNodeIds.contains(it.id) ||
        // 3. or implicitly as the shared node between a footway tagged with footway = crossing and
        //    another tagged with footway = sidewalk that is the continuation of the way and is not
        //    and intersection (thus, has exactly two connections: to the sidewalk and to the crossing)
        crossingEndNodesConnectionCountByIds[it.id] == 2
    }
}
