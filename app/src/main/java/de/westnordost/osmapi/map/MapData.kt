package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.*
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.util.MultiIterable

interface MapDataWithGeometry : MapData {
    fun getNodeGeometry(id: Long): ElementPointGeometry?
    fun getWayGeometry(id: Long): ElementGeometry?
    fun getRelationGeometry(id: Long): ElementGeometry?

    fun getGeometry(elementType: Element.Type, id: Long): ElementGeometry? = when(elementType) {
        Element.Type.NODE -> getNodeGeometry(id)
        Element.Type.WAY -> getWayGeometry(id)
        Element.Type.RELATION -> getRelationGeometry(id)
    }
}

interface MapData : Iterable<Element> {
    val nodes: Collection<Node>
    val ways: Collection<Way>
    val relations: Collection<Relation>
    val boundingBox: BoundingBox?

    fun getNode(id: Long): Node?
    fun getWay(id: Long): Way?
    fun getRelation(id: Long): Relation?
}

open class MutableMapData : MapData, MapDataHandler {

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
        val elements = MultiIterable<Element>()
        elements.add(nodes)
        elements.add(ways)
        elements.add(relations)
        return elements.iterator()
    }
}

fun MapData.isRelationComplete(id: Long): Boolean =
    getRelation(id)?.members?.all { member ->
        when (member.type!!) {
            Element.Type.NODE -> getNode(member.ref) != null
            Element.Type.WAY -> getWay(member.ref) != null && isWayComplete(member.ref)
            /* not being recursive here is deliberate. sub-relations are considered not relevant
               for the element geometry in StreetComplete (and OSM API call to get a "complete"
               relation also does not include sub-relations) */
            Element.Type.RELATION -> getRelation(member.ref) != null
        }
    } ?: false

fun MapData.isWayComplete(id: Long): Boolean =
    getWay(id)?.nodeIds?.all { getNode(it) != null } ?: false