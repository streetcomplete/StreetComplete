package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.BoundingBox as OsmApiBoundingBox
import de.westnordost.osmapi.map.data.Node as OsmApiNode
import de.westnordost.osmapi.map.data.Relation as OsmApiRelation
import de.westnordost.osmapi.map.data.Way as OsmApiWay
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.osmnotes.toBoundingBox
import de.westnordost.streetcomplete.data.toNode
import de.westnordost.streetcomplete.data.toRelation
import de.westnordost.streetcomplete.data.toWay

open class MutableMapData() : MapData, MapDataHandler {

    constructor(other: Iterable<Element>) : this() {
        addAll(other)
    }

    protected val nodesById: MutableMap<Long, Node> = mutableMapOf()
    protected val waysById: MutableMap<Long, Way> = mutableMapOf()
    protected val relationsById: MutableMap<Long, Relation> = mutableMapOf()
    override var boundingBox: BoundingBox? = null
        protected set

    fun handle(bounds: BoundingBox) { boundingBox = bounds }

    override fun handle(bounds: OsmApiBoundingBox) { boundingBox = bounds.toBoundingBox() }
    override fun handle(node: OsmApiNode) { nodesById[node.id] = node.toNode() }
    override fun handle(way: OsmApiWay) { waysById[way.id] = way.toWay() }
    override fun handle(relation: OsmApiRelation) { relationsById[relation.id] = relation.toRelation() }

    override val nodes get() = nodesById.values
    override val ways get() = waysById.values
    override val relations get() = relationsById.values

    override fun getNode(id: Long) = nodesById[id]
    override fun getWay(id: Long) = waysById[id]
    override fun getRelation(id: Long) = relationsById[id]

    fun addAll(elements: Iterable<Element>) {
        for (element in elements) {
            when(element) {
                is Node -> nodesById[element.id] = element
                is Way -> waysById[element.id] = element
                is Relation -> relationsById[element.id] = element
            }
        }
    }

    override fun iterator(): Iterator<Element> {
        return (nodes.asSequence() + ways.asSequence() + relations.asSequence()).iterator()
    }
}
