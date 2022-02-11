package de.westnordost.streetcomplete.data.osm.geometry

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.NodeDao
import de.westnordost.streetcomplete.data.osm.mapdata.toElementIds
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
        if (keys.isEmpty()) return emptyList()
        val elementIds = keys.toElementIds()
        val results = ArrayList<ElementGeometryEntry>(elementIds.size)
        results.addAll(nodeDao.getAllEntries(elementIds.nodes))
        results.addAll(wayGeometryDao.getAllEntries(elementIds.ways))
        results.addAll(relationGeometryDao.getAllEntries(elementIds.relations))
        return results
    }

    fun deleteAll(keys: Collection<ElementKey>): Int {
        val elementIds = keys.toElementIds()
        return wayGeometryDao.deleteAll(elementIds.ways) +
            relationGeometryDao.deleteAll(elementIds.relations)
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
