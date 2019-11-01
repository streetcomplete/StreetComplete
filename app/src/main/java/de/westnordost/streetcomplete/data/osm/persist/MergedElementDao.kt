package de.westnordost.streetcomplete.data.osm.persist

import javax.inject.Inject

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way

class MergedElementDao @Inject
constructor(
    private val nodeDao: NodeDao,
    private val wayDao: WayDao,
    private val relationDao: RelationDao
) {

    fun putAll(elements: Collection<Element>) {
        val nodes = mutableListOf<Node>()
        val ways = mutableListOf<Way>()
        val relations = mutableListOf<Relation>()

        for (element in elements) {
            when (element) {
                is Node -> nodes.add(element)
                is Way -> ways.add(element)
                is Relation -> relations.add(element)
            }
        }
        if (nodes.isNotEmpty()) nodeDao.putAll(nodes)
        if (ways.isNotEmpty()) wayDao.putAll(ways)
        if (relations.isNotEmpty()) relationDao.putAll(relations)
    }

    fun put(element: Element) {
        when (element) {
            is Node -> nodeDao.put(element)
            is Way -> wayDao.put(element)
            is Relation -> relationDao.put(element)
        }
    }

    fun delete(type: Element.Type, id: Long) {
        when (type) {
            Element.Type.NODE -> nodeDao.delete(id)
            Element.Type.WAY -> wayDao.delete(id)
            Element.Type.RELATION -> relationDao.delete(id)
        }
    }

    fun get(type: Element.Type, id: Long): Element? {
        return when (type) {
            Element.Type.NODE -> nodeDao.get(id)
            Element.Type.WAY -> wayDao.get(id)
            Element.Type.RELATION -> relationDao.get(id)
        }
    }

    fun deleteUnreferenced() {
        nodeDao.deleteUnreferenced()
        wayDao.deleteUnreferenced()
        relationDao.deleteUnreferenced()
    }
}
