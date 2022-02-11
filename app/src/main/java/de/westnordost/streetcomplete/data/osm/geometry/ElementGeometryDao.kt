package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.NodeDao
import de.westnordost.streetcomplete.data.osm.mapdata.toElementIds

/** Stores the geometry of elements. Actually, stores nothing, but delegates the work to
 *  WayGeometryDao and RelationDao. Node geometry is never stored separately, but created
 *  from node position. */
class ElementGeometryDao(
    private val nodeDao: NodeDao,
    private val wayGeometryDao: WayGeometryDao,
    private val relationGeometryDao: RelationGeometryDao
) {
    fun put(entry: ElementGeometryEntry) {
        when (entry.elementType) {
            ElementType.NODE -> Unit
            ElementType.WAY -> wayGeometryDao.put(entry)
            ElementType.RELATION -> relationGeometryDao.put(entry)
        }
    }

    fun get(type: ElementType, id: Long): ElementGeometry? =
        when (type) {
            ElementType.NODE -> nodeDao.get(id)?.let { ElementPointGeometry(it.position) }
            ElementType.WAY -> wayGeometryDao.get(id)
            ElementType.RELATION -> relationGeometryDao.get(id)
        }

    fun delete(type: ElementType, id: Long): Boolean =
        when (type) {
            ElementType.NODE -> false
            ElementType.WAY -> wayGeometryDao.delete(id)
            ElementType.RELATION -> relationGeometryDao.delete(id)
        }

    fun putAll(entries: Collection<ElementGeometryEntry>) {
        wayGeometryDao.putAll(entries.filter { it.elementType == ElementType.WAY })
        relationGeometryDao.putAll(entries.filter { it.elementType == ElementType.RELATION })
    }

    fun getAllKeys(bbox: BoundingBox): List<ElementKey> {
        val results = mutableListOf<ElementKey>()
        results.addAll(nodeDao.getAllIds(bbox).map { ElementKey(ElementType.NODE, it) })
        results.addAll(wayGeometryDao.getAllKeys(bbox))
        results.addAll(relationGeometryDao.getAllKeys(bbox))
        return results
    }

    fun getAllEntries(bbox: BoundingBox): List<ElementGeometryEntry> =
        nodeDao.getAllEntries(bbox) + getAllEntriesWithoutNodes(bbox)

    fun getAllEntriesWithoutNodes(bbox: BoundingBox): List<ElementGeometryEntry> {
        val results = mutableListOf<ElementGeometryEntry>()
        results.addAll(wayGeometryDao.getAllEntries(bbox))
        results.addAll(relationGeometryDao.getAllEntries(bbox))
        return results
    }

    fun getAllEntries(keys: Collection<ElementKey>): List<ElementGeometryEntry> {
        if (keys.isEmpty()) return emptyList()
        val elementIds = keys.toElementIds()
        val results = mutableListOf<ElementGeometryEntry>()
        results.addAll(nodeDao.getAllEntries(elementIds.nodes))
        results.addAll(wayGeometryDao.getAllEntries(elementIds.ways))
        results.addAll(relationGeometryDao.getAllEntries(elementIds.relations))
        return results
    }

    fun deleteAll(keys: Collection<ElementKey>): Int {
        if (keys.isEmpty()) return 0
        val elementIds = keys.toElementIds()
        var deletedCount = 0
        deletedCount += wayGeometryDao.deleteAll(elementIds.ways)
        deletedCount += relationGeometryDao.deleteAll(elementIds.relations)
        return deletedCount
    }

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
