package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.handler.MapDataHandler

open class MutableMapData() : MapData, MapDataHandler {

    constructor(other: Iterable<Element>) : this() {
        addAll(other)
    }

    protected val nodesById: MutableMap<Long, Node> = mutableMapOf()
    protected val waysById: MutableMap<Long, Way> = mutableMapOf()
    protected val relationsById: MutableMap<Long, Relation> = mutableMapOf()
    override var boundingBox: BoundingBox? = null
        protected set

    override fun handle(bounds: BoundingBox) { boundingBox = bounds }
    override fun handle(node: Node) { nodesById[node.id] = node }
    override fun handle(way: Way) { waysById[way.id] = way }
    override fun handle(relation: Relation) { relationsById[relation.id] = relation }

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
