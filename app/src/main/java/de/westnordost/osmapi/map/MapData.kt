package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.util.MultiIterable

data class MapData(
    val nodes: MutableMap<Long, Node> = mutableMapOf(),
    val ways: MutableMap<Long, Way> = mutableMapOf(),
    val relations: MutableMap<Long, Relation> = mutableMapOf()) : MapDataHandler, Iterable<Element> {

    override fun handle(bounds: BoundingBox) {}
    override fun handle(node: Node) { nodes[node.id] = node }
    override fun handle(way: Way) { ways[way.id] = way }
    override fun handle(relation: Relation) { relations[relation.id] = relation }

    override fun iterator(): Iterator<Element> {
        val elements = MultiIterable<Element>()
        elements.add(nodes.values)
        elements.add(ways.values)
        elements.add(relations.values)
        return elements.iterator()
    }

    fun add(other: MapData) {
        nodes.putAll(other.nodes)
        ways.putAll(other.ways)
        relations.putAll(other.relations)
    }
}