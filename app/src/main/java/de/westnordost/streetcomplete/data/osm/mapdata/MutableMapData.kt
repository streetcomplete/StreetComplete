package de.westnordost.streetcomplete.data.osm.mapdata

open class MutableMapData() : MapData {

    constructor(other: Iterable<Element>) : this() {
        addAll(other)
    }

    protected val nodesById: MutableMap<Long, Node> = mutableMapOf()
    protected val waysById: MutableMap<Long, Way> = mutableMapOf()
    protected val relationsById: MutableMap<Long, Relation> = mutableMapOf()
    override var boundingBox: BoundingBox? = null

    override val nodes get() = nodesById.values
    override val ways get() = waysById.values
    override val relations get() = relationsById.values

    override fun getNode(id: Long) = nodesById[id]
    override fun getWay(id: Long) = waysById[id]
    override fun getRelation(id: Long) = relationsById[id]

    fun addAll(elements: Iterable<Element>) {
        elements.forEach(this::add)
    }

    fun add(element: Element) {
        when (element) {
            is Node -> nodesById[element.id] = element
            is Way -> waysById[element.id] = element
            is Relation -> relationsById[element.id] = element
        }
    }

    override fun iterator(): Iterator<Element> =
        (nodes.asSequence() + ways.asSequence() + relations.asSequence()).iterator()
}
