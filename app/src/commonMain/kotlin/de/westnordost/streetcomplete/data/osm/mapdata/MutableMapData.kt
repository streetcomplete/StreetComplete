package de.westnordost.streetcomplete.data.osm.mapdata

open class MutableMapData(
    nodes: Collection<Node> = emptyList(),
    ways: Collection<Way> = emptyList(),
    relations: Collection<Relation> = emptyList()
) : MapData {

    constructor(other: Iterable<Element>) : this() {
        addAll(other)
    }

    private val nodesById: MutableMap<Long, Node> = nodes.associateByTo(HashMap()) { it.id }
    private val waysById: MutableMap<Long, Way> = ways.associateByTo(HashMap()) { it.id }
    private val relationsById: MutableMap<Long, Relation> = relations.associateByTo(HashMap()) { it.id }
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
