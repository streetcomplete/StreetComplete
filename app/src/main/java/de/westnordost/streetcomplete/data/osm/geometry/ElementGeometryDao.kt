package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.NodeDao
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.RELATION
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.WAY

/** Stores the geometry of elements. Actually, stores nothing, but delegates the work to
 *  WayGeometryDao and RelationDao. Node geometry is never stored separately, but created
 *  from node position. */
class ElementGeometryDao(
    private val nodeDao: NodeDao,
    private val wayGeometryDao: WayGeometryDao,
    private val relationGeometryDao: RelationGeometryDao
) {
    fun put(entry: ElementGeometryEntry) = when (entry.elementType) {
        NODE -> Unit
        WAY -> wayGeometryDao.put(entry)
        RELATION -> relationGeometryDao.put(entry)
    }

    fun get(type: ElementType, id: Long): ElementGeometry? = when (type) {
        NODE -> nodeDao.get(id)?.let { ElementPointGeometry(it.position) }
        WAY -> wayGeometryDao.get(id)
        RELATION -> relationGeometryDao.get(id)
    }

    fun delete(type: ElementType, id: Long): Boolean = when (type) {
        NODE -> false
        WAY -> wayGeometryDao.delete(id)
        RELATION -> relationGeometryDao.delete(id)
    }

    fun putAll(entries: Collection<ElementGeometryEntry>) {
        wayGeometryDao.putAll(entries.filter { it.elementType == WAY })
        relationGeometryDao.putAll(entries.filter { it.elementType == RELATION })
    }

    fun getAllKeys(bbox: BoundingBox): List<ElementKey> {
        val results = mutableListOf<ElementKey>()
        results.addAll(nodeDao.getAllIds(bbox).map { ElementKey(NODE, it) })
        results.addAll(wayGeometryDao.getAllIds(bbox).map { ElementKey(WAY, it) })
        results.addAll(relationGeometryDao.getAllIds(bbox).map { ElementKey(RELATION, it) })
        return results
    }

    fun getAllEntries(bbox: BoundingBox): List<ElementGeometryEntry> =
        nodeDao.getAllEntries(bbox) + getAllEntriesWithoutNodes(bbox)

    fun getAllEntriesWithoutNodes(bbox: BoundingBox): List<ElementGeometryEntry> =
        wayGeometryDao.getAllEntries(bbox) + relationGeometryDao.getAllEntries(bbox)

    fun getAllEntries(keys: Collection<ElementKey>): List<ElementGeometryEntry> {
        val results = ArrayList<ElementGeometryEntry>(keys.size)
        results.addAll(nodeDao.getAllEntries(keys.filterByType(NODE)))
        results.addAll(wayGeometryDao.getAllEntries(keys.filterByType(WAY)))
        results.addAll(relationGeometryDao.getAllEntries(keys.filterByType(RELATION)))
        return results
    }

    fun deleteAll(keys: Collection<ElementKey>): Int =
        wayGeometryDao.deleteAll(keys.filterByType(WAY)) +
        relationGeometryDao.deleteAll(keys.filterByType(RELATION))

    fun clear() {
        wayGeometryDao.clear()
        relationGeometryDao.clear()
    }
}

data class ElementGeometryEntry(
    val elementType: ElementType,
    val elementId: Long,
    val geometry: ElementGeometry
)

private fun Iterable<ElementKey>.filterByType(type: ElementType) =
    mapNotNull { if (it.type == type) it.id else null }
