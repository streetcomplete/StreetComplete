package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Node
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

private val footwaysFilter by lazy {"""
    ways with (
        highway ~ footway|path
        or highway = cycleway and foot ~ yes|designated
        or highway = path
      )
      and access !~ private|no and foot !~ no
""".toElementFilterExpression() }

fun MapData.findAllKerbNodes(): Iterable<Node> {
    val footwayNodes = mutableSetOf<Node>()
    ways.filter { footwaysFilter.matches(it) }
        .flatMap { it.nodeIds }
        .mapNotNullTo(footwayNodes) { getNode(it) }

    val kerbBarrierNodeIds = mutableSetOf<Long>()
    ways.filter { it.tags?.get("barrier") == "kerb" }
        .flatMapTo(kerbBarrierNodeIds) { it.nodeIds }

    // Kerbs can be defined in two ways:
    return footwayNodes.filter {
        // 1. either as the intersection of a way tagged with barrier = kerb that crosses a footway
        it.tags?.get("barrier") == "kerb" ||
        // 2. or as a node tagged with barrier = kerb on a footway
        kerbBarrierNodeIds.contains(it.id)
    }
}