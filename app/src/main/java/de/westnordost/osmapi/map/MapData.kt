package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.handler.MapDataHandler

data class MapData(
    val nodes: MutableMap<Long, Node> = mutableMapOf(),
    val ways: MutableMap<Long, Way> = mutableMapOf(),
    val relations: MutableMap<Long, Relation> = mutableMapOf()) : MapDataHandler {

    override fun handle(bounds: BoundingBox) {}
    override fun handle(node: Node) { nodes[node.id] = node }
    override fun handle(way: Way) { ways[way.id] = way }
    override fun handle(relation: Relation) { relations[relation.id] = relation }
}