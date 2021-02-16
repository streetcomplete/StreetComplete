package de.westnordost.streetcomplete.data.osm.mapdata

import javax.inject.Inject

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.data.Element.Type.*

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

    fun get(type: Element.Type, id: Long): Element? {
        return when (type) {
            NODE -> nodeDao.get(id)
            WAY -> wayDao.get(id)
            RELATION -> relationDao.get(id)
        }
    }

    fun delete(type: Element.Type, id: Long) {
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
        val result = mutableListOf<Element>()
        result.addAll(nodeDao.getAll(keys.filter { it.elementType == NODE }.map { it.elementId }))
        result.addAll(wayDao.getAll(keys.filter { it.elementType == WAY }.map { it.elementId }))
        result.addAll(relationDao.getAll(keys.filter { it.elementType == RELATION }.map { it.elementId }))
        return result
    }

    fun deleteAll(keys: Iterable<ElementKey>) {
        nodeDao.deleteAll(keys.filter { it.elementType == NODE }.map { it.elementId })
        wayDao.deleteAll(keys.filter { it.elementType == WAY }.map { it.elementId })
        relationDao.deleteAll(keys.filter { it.elementType == RELATION }.map { it.elementId })
    }

    fun getIdsOlderThan(timestamp: Long): List<ElementKey> {
        return nodeDao.getIdsOlderThan(timestamp).map { ElementKey(NODE, it) } +
            wayDao.getIdsOlderThan(timestamp).map { ElementKey(WAY, it) } +
            relationDao.getIdsOlderThan(timestamp).map { ElementKey(RELATION, it) }
    }
}
