package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY
import javax.inject.Inject

/** Stores OSM elements. Actually, stores nothing, but delegates the work to a NodeDao, WayDao and
 *  a RelationDao. :-P */
class ElementDao @Inject constructor(
    private val nodeDao: NodeDao,
    private val wayDao: WayDao,
    private val relationDao: RelationDao
) {
    fun put(element: Element) {
        when (element) {
            is Node -> nodeDao.put(element)
            is Way -> wayDao.put(element)
            is Relation -> relationDao.put(element)
        }
    }

    fun get(type: ElementType, id: Long): Element? {
        return when (type) {
            NODE -> nodeDao.get(id)
            WAY -> wayDao.get(id)
            RELATION -> relationDao.get(id)
        }
    }

    fun delete(type: ElementType, id: Long) {
        when (type) {
            NODE -> nodeDao.delete(id)
            WAY -> wayDao.delete(id)
            RELATION -> relationDao.delete(id)
        }
    }

    fun putAll(elements: Iterable<Element>) {
        nodeDao.putAll(elements.filterIsInstance<Node>())
        wayDao.putAll(elements.filterIsInstance<Way>())
        relationDao.putAll(elements.filterIsInstance<Relation>())
    }

    fun getAll(keys: Iterable<ElementKey>): List<Element> {
        val elementIds = keys.toElementIds()
        if (elementIds.size == 0) return emptyList()

        val result = ArrayList<Element>(elementIds.size)
        result.addAll(nodeDao.getAll(elementIds.nodes))
        result.addAll(wayDao.getAll(elementIds.ways))
        result.addAll(relationDao.getAll(elementIds.relations))
        return result
    }

    fun deleteAll(keys: Iterable<ElementKey>): Int {
        val elementIds = keys.toElementIds()
        if (elementIds.size == 0) return 0

        // delete first relations, then ways, then nodes because relations depend on ways depend on nodes
        return relationDao.deleteAll(elementIds.relations) +
            wayDao.deleteAll(elementIds.ways) +
            nodeDao.deleteAll(elementIds.nodes)
    }

    fun clear() {
        relationDao.clear()
        wayDao.clear()
        nodeDao.clear()
    }

    fun getIdsOlderThan(timestamp: Long, limit: Int? = null): List<ElementKey> {
        val result = mutableListOf<ElementKey>()
        // get relations first, then ways, then nodes because relations depend on ways depend on nodes.
        result.addAll(relationDao.getIdsOlderThan(timestamp, limit?.minus(result.size)).map { ElementKey(RELATION, it) })
        result.addAll(wayDao.getIdsOlderThan(timestamp, limit?.minus(result.size)).map { ElementKey(WAY, it) })
        result.addAll(nodeDao.getIdsOlderThan(timestamp, limit?.minus(result.size)).map { ElementKey(NODE, it) })
        return result
    }

}

private data class ElementIds(val nodes: List<Long>, val ways: List<Long>, val relations: List<Long>) {
    val size: Int get() = nodes.size + ways.size + relations.size
}

private fun Iterable<ElementKey>.toElementIds(): ElementIds {
    val nodes = ArrayList<Long>()
    val ways = ArrayList<Long>()
    val relations = ArrayList<Long>()
    for (key in this) {
        when(key.type) {
            NODE -> nodes.add(key.id)
            WAY -> ways.add(key.id)
            RELATION -> relations.add(key.id)
        }
    }
    return ElementIds(nodes, ways, relations)
}
