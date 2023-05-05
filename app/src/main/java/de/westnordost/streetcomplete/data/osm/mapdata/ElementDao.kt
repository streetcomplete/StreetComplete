package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY

/** Stores OSM elements. Actually, stores nothing, but delegates the work to a NodeDao, WayDao and
 *  a RelationDao. :-P */
class ElementDao(
    private val nodeDao: NodeDao,
    private val wayDao: WayDao,
    private val relationDao: RelationDao
) {
    fun put(element: Element) = when (element) {
        is Node -> nodeDao.put(element)
        is Way -> wayDao.put(element)
        is Relation -> relationDao.put(element)
    }

    fun get(type: ElementType, id: Long): Element? = when (type) {
        NODE -> nodeDao.get(id)
        WAY -> wayDao.get(id)
        RELATION -> relationDao.get(id)
    }

    fun delete(type: ElementType, id: Long) = when (type) {
        NODE -> nodeDao.delete(id)
        WAY -> wayDao.delete(id)
        RELATION -> relationDao.delete(id)
    }

    fun putAll(elements: Iterable<Element>) {
        nodeDao.putAll(elements.filterIsInstance<Node>())
        wayDao.putAll(elements.filterIsInstance<Way>())
        relationDao.putAll(elements.filterIsInstance<Relation>())
    }

    fun getAll(keys: Collection<ElementKey>): List<Element> {
        val result = ArrayList<Element>(keys.size)
        result.addAll(nodeDao.getAll(keys.filterByType(NODE)))
        result.addAll(wayDao.getAll(keys.filterByType(WAY)))
        result.addAll(relationDao.getAll(keys.filterByType(RELATION)))
        return result
    }

    fun getAll(bbox: BoundingBox): List<Element> {
        val nodes = nodeDao.getAll(bbox)
        val nodeIds = nodes.map { it.id }.toSet()
        val ways = wayDao.getAllForNodes(nodeIds)
        val wayIds = ways.map { it.id }
        val additionalWayNodeIds = ways
            .asSequence()
            .flatMap { it.nodeIds }
            .filter { it !in nodeIds }
            .toList()
        val additionalNodes = nodeDao.getAll(additionalWayNodeIds)
        val relations = relationDao.getAllForElements(nodeIds = additionalWayNodeIds + nodeIds, wayIds = wayIds)
        val result = ArrayList<Element>(nodes.size + additionalNodes.size + ways.size + relations.size)
        result.addAll(nodes)
        result.addAll(additionalNodes)
        result.addAll(ways)
        result.addAll(relations)
        return result
    }

    fun getAllKeys(bbox: BoundingBox): List<ElementKey> {
        val nodeIds = nodeDao.getAllIds(bbox)
        val wayIds = wayDao.getAllIdsForNodes(nodeIds)
        val relationIds = relationDao.getAllIdsForElements(nodeIds = nodeIds, wayIds = wayIds)
        val result = ArrayList<ElementKey>(nodeIds.size + wayIds.size + relationIds.size)
        result.addAll(nodeIds.map { ElementKey(NODE, it) })
        result.addAll(wayIds.map { ElementKey(WAY, it) })
        result.addAll(relationIds.map { ElementKey(RELATION, it) })
        return result
    }

    fun deleteAll(keys: Iterable<ElementKey>): Int =
        // delete first relations, then ways, then nodes because relations depend on ways depend on nodes
        relationDao.deleteAll(keys.filterByType(RELATION)) +
        wayDao.deleteAll(keys.filterByType(WAY)) +
        nodeDao.deleteAll(keys.filterByType(NODE))

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

private fun Iterable<ElementKey>.filterByType(type: ElementType) =
    mapNotNull { if (it.type == type) it.id else null }
