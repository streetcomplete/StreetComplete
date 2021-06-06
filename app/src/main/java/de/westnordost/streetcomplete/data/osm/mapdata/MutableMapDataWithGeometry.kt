package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry

class MutableMapDataWithGeometry() : MapDataWithGeometry {

    constructor(elements: Iterable<Element>, geometryEntries: Iterable<ElementGeometryEntry>) : this() {
        putAll(elements, geometryEntries)
    }

    private val nodesById = HashMap<Long, Node>()
    private val waysById = HashMap<Long, Way>()
    private val relationsById = HashMap<Long, Relation>()
    private val nodeGeometriesById = HashMap<Long, ElementPointGeometry?>()
    private val wayGeometriesById = HashMap<Long, ElementGeometry?>()
    private val relationGeometriesById = HashMap<Long, ElementGeometry?>()

    override var boundingBox: BoundingBox? = null

    override fun getNodeGeometry(id: Long) = nodeGeometriesById[id]
    override fun getWayGeometry(id: Long) = wayGeometriesById[id]
    override fun getRelationGeometry(id: Long) = relationGeometriesById[id]

    override val nodes get() = nodesById.values
    override val ways get() = waysById.values
    override val relations get() = relationsById.values

    override fun getNode(id: Long) = nodesById[id]
    override fun getWay(id: Long) = waysById[id]
    override fun getRelation(id: Long) = relationsById[id]

    fun put(element: Element, geometry: ElementGeometry?) {
        putElement(element)
        putGeometry(element.type, element.id, geometry)
    }

    fun remove(type: ElementType, id: Long) {
        when(type) {
            ElementType.NODE -> {
                nodesById.remove(id)
                nodeGeometriesById.remove(id)
            }
            ElementType.WAY -> {
                waysById.remove(id)
                wayGeometriesById.remove(id)
            }
            ElementType.RELATION -> {
                relationsById.remove(id)
                relationGeometriesById.remove(id)
            }
        }
    }

    fun putAll(elements: Iterable<Element>, geometryEntries: Iterable<ElementGeometryEntry>) {
        for (element in elements) {
            putElement(element)
        }
        for (entry in geometryEntries) {
            putGeometry(entry.elementType, entry.elementId, entry.geometry)
        }
    }

    fun putElement(element: Element) {
        val id = element.id
        when(element) {
            is Node -> nodesById[id] = element
            is Way ->  waysById[id] = element
            is Relation -> relationsById[id] = element
        }
    }

    fun putGeometry(type: ElementType, id: Long, geometry: ElementGeometry?) {
        when(type) {
            ElementType.NODE -> nodeGeometriesById[id] = geometry as? ElementPointGeometry
            ElementType.WAY -> wayGeometriesById[id] = geometry
            ElementType.RELATION -> relationGeometriesById[id] = geometry
        }
    }

    override fun iterator(): Iterator<Element> {
        return (nodes.asSequence() + ways.asSequence() + relations.asSequence()).iterator()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MutableMapDataWithGeometry) return false

        return boundingBox == other.boundingBox
            && nodesById == other.nodesById
            && waysById == other.waysById
            && relationsById == other.relationsById
            && nodeGeometriesById == other.nodeGeometriesById
            && wayGeometriesById == other.wayGeometriesById
            && relationGeometriesById == other.relationGeometriesById
    }

    override fun hashCode(): Int {
        var result = nodesById.hashCode()
        result = 31 * result + waysById.hashCode()
        result = 31 * result + relationsById.hashCode()
        result = 31 * result + nodeGeometriesById.hashCode()
        result = 31 * result + wayGeometriesById.hashCode()
        result = 31 * result + relationGeometriesById.hashCode()
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        return result
    }
}
