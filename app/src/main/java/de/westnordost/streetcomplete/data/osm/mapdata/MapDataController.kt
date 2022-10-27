package de.westnordost.streetcomplete.data.osm.mapdata

import android.util.Log
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import java.util.concurrent.CopyOnWriteArrayList

/** Controller to access element data and its geometry and handle updates to it (from OSM API) */
class MapDataController internal constructor(
    private val nodeDB: NodeDao,
    private val wayDB: WayDao,
    private val relationDB: RelationDao,
    private val elementDB: ElementDao,
    private val geometryDB: ElementGeometryDao,
    private val elementGeometryCreator: ElementGeometryCreator,
    private val createdElementsController: CreatedElementsController
) : MapDataRepository {

    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        /** Called when a number of elements have been updated or deleted */
        fun onUpdated(updated: MutableMapDataWithGeometry, deleted: Collection<ElementKey>)

        /** Called when all elements in the given bounding box should be replaced with the elements
         *  in the mapDataWithGeometry */
        fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MutableMapDataWithGeometry)

        /** Called when all elements have been cleared */
        fun onCleared()
    }
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    private val cache = MapDataCache(
        SPATIAL_CACHE_TILE_ZOOM,
        SPATIAL_CACHE_TILES,
        SPATIAL_CACHE_INITIAL_CAPACITY
    ) { bbox ->
        val elements = elementDB.getAll(bbox)
        val elementGeometries = geometryDB.getAllEntries(
            elements.mapNotNull { if (it !is Node) ElementKey(it.type, it.id) else null }
        )
        elements to elementGeometries
    }

    /** update element data with [mapData] in the given [bbox] (fresh data from the OSM API has been
     *  downloaded) */
    fun putAllForBBox(bbox: BoundingBox, mapData: MutableMapData) {
        val time = nowAsEpochMilliseconds()

        val oldElementKeys: Set<ElementKey>
        val geometryEntries: Collection<ElementGeometryEntry>
        synchronized(this) {
            // for incompletely downloaded relations, complete the map data (as far as possible) with
            // local data, i.e. with local nodes and ways (still) in local storage
            completeMapData(mapData)

            geometryEntries = createGeometries(mapData, mapData)

            // don't use cache here, because if not everything is already cached, db call will be faster
            oldElementKeys = elementDB.getAllKeys(mapData.boundingBox!!).toMutableSet()
            for (element in mapData) {
                oldElementKeys.remove(ElementKey(element.type, element.id))
            }

            // for the cache, use bbox and not mapData.boundingBox because the latter is padded,
            // see comment for QUEST_FILTER_PADDING
            cache.update(oldElementKeys, mapData, geometryEntries, bbox)

            elementDB.deleteAll(oldElementKeys)
            geometryDB.deleteAll(oldElementKeys)
            geometryDB.putAll(geometryEntries)
            elementDB.putAll(mapData)
        }

        Log.i(TAG,
            "Persisted ${geometryEntries.size} and deleted ${oldElementKeys.size} elements and geometries" +
            " in ${((nowAsEpochMilliseconds() - time) / 1000.0).format(1)}s"
        )

        val mapDataWithGeometry = MutableMapDataWithGeometry(mapData, geometryEntries)
        mapDataWithGeometry.boundingBox = mapData.boundingBox

        onReplacedForBBox(bbox, mapDataWithGeometry)
    }

    /** incorporate the [mapDataUpdates] (data has been updated after upload) */
    fun updateAll(mapDataUpdates: MapDataUpdates) {
        val elements = mapDataUpdates.updated
        // need mapData in order to create (updated) geometry
        val mapData = MutableMapData(elements)

        val deletedKeys: List<ElementKey>
        val geometryEntries: Collection<ElementGeometryEntry>
        synchronized(this) {
            completeMapData(mapData)

            geometryEntries = createGeometries(elements, mapData)

            val newElementKeys = mapDataUpdates.idUpdates.map { ElementKey(it.elementType, it.newElementId) }
            val oldElementKeys = mapDataUpdates.idUpdates.map { ElementKey(it.elementType, it.oldElementId) }
            deletedKeys = mapDataUpdates.deleted + oldElementKeys

            cache.update(deletedKeys, elements, geometryEntries)

            elementDB.deleteAll(deletedKeys)
            geometryDB.deleteAll(deletedKeys)
            geometryDB.putAll(geometryEntries)
            elementDB.putAll(elements)
            createdElementsController.putAll(newElementKeys)
        }

        val mapDataWithGeom = MutableMapDataWithGeometry(elements, geometryEntries)
        mapDataWithGeom.boundingBox = mapData.boundingBox

        onUpdated(updated = mapDataWithGeom, deleted = deletedKeys)
    }

    /** Create ElementGeometryEntries for [elements] using [mapData] to supply the necessary geometry */
    private fun createGeometries(elements: Iterable<Element>, mapData: MapData): Collection<ElementGeometryEntry> =
        elements.mapNotNull { element ->
            val geometry = elementGeometryCreator.create(element, mapData, true)
            geometry?.let { ElementGeometryEntry(element.type, element.id, geometry) }
        }

    private fun completeMapData(mapData: MutableMapData) {
        val missingNodeIds = mutableListOf<Long>()
        val missingWayIds = mutableListOf<Long>()
        for (relation in mapData.relations) {
            for (member in relation.members) {
                if (member.type == ElementType.NODE && mapData.getNode(member.ref) == null) {
                    missingNodeIds.add(member.ref)
                }
                if (member.type == ElementType.WAY && mapData.getWay(member.ref) == null) {
                    missingWayIds.add(member.ref)
                }
                /* deliberately not recursively looking for relations of relations
                   because that is also not how the OSM API works */
            }
        }

        val ways = getWays(missingWayIds)
        for (way in mapData.ways + ways) {
            for (nodeId in way.nodeIds) {
                if (mapData.getNode(nodeId) == null) {
                    missingNodeIds.add(nodeId)
                }
            }
        }
        val nodes = getNodes(missingNodeIds)

        mapData.addAll(nodes)
        mapData.addAll(ways)
    }

    fun get(type: ElementType, id: Long): Element? = cache.getElement(type, id, elementDB::get)

    fun getGeometry(type: ElementType, id: Long): ElementGeometry? = cache.getGeometry(type, id, geometryDB::get)

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> = cache.getGeometries(keys, geometryDB::getAllEntries)

    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry {
        val time = nowAsEpochMilliseconds()
        val result = cache.getMapDataWithGeometry(bbox)
        Log.i(TAG, "Fetched ${result.size} elements and geometries in ${nowAsEpochMilliseconds() - time}ms")

        return result
    }

    data class ElementCounts(val nodes: Int, val ways: Int, val relations: Int)
    // this is used after downloading one tile with auto-download, so we should always have it cached
    fun getElementCounts(bbox: BoundingBox): ElementCounts {
        val data = getMapDataWithGeometry(bbox)
        return ElementCounts(
            data.count { it is Node },
            data.count { it is Way },
            data.count { it is Relation }
        )
    }

    override fun getNode(id: Long): Node? = get(ElementType.NODE, id) as? Node
    override fun getWay(id: Long): Way? = get(ElementType.WAY, id) as? Way
    override fun getRelation(id: Long): Relation? = get(ElementType.RELATION, id) as? Relation

    fun getAll(elementKeys: Collection<ElementKey>): List<Element> =
        cache.getElements(elementKeys, elementDB::getAll)

    fun getNodes(ids: Collection<Long>): List<Node> = cache.getNodes(ids, nodeDB::getAll)
    fun getWays(ids: Collection<Long>): List<Way> = cache.getWays(ids, wayDB::getAll)
    fun getRelations(ids: Collection<Long>): List<Relation> = cache.getRelations(ids, relationDB::getAll)

    override fun getWaysForNode(id: Long): List<Way> = cache.getWaysForNode(id, wayDB::getAllForNode)
    override fun getRelationsForNode(id: Long): List<Relation> = cache.getRelationsForNode(id, relationDB::getAllForNode)
    override fun getRelationsForWay(id: Long): List<Relation> = cache.getRelationsForWay(id, relationDB::getAllForWay)
    override fun getRelationsForRelation(id: Long): List<Relation> = cache.getRelationsForRelation(id, relationDB::getAllForRelation)

    override fun getWayComplete(id: Long): MapData? {
        val way = getWay(id) ?: return null
        val nodeIds = way.nodeIds.toSet()
        val nodes = getNodes(nodeIds)
        if (nodes.size < nodeIds.size) return null
        return MutableMapData(nodes + way)
    }

    override fun getRelationComplete(id: Long): MapData? {
        val relation = getRelation(id) ?: return null
        val elementKeys = relation.members.map { ElementKey(it.type, it.ref) }.toSet()
        val elements = getAll(elementKeys)
        if (elements.size < elementKeys.size) return null
        return MutableMapData(elements + relation)
    }

    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int {
        val elements: List<ElementKey>
        val elementCount: Int
        val geometryCount: Int
        synchronized(this) {
            elements = elementDB.getIdsOlderThan(timestamp, limit)
            if (elements.isEmpty()) return 0

            cache.update(deletedKeys = elements)
            elementCount = elementDB.deleteAll(elements)
            geometryCount = geometryDB.deleteAll(elements)
            createdElementsController.deleteAll(elements)
        }
        Log.i(TAG, "Deleted $elementCount old elements and $geometryCount geometries")

        onUpdated(deleted = elements)

        return elementCount
    }

    fun clear() {
        synchronized(this) {
            clearCache()
            elementDB.clear()
            geometryDB.clear()
            createdElementsController.clear()
        }
        onCleared()
    }

    fun clearCache() = synchronized(this) { cache.clear() }

    fun trimCache() = synchronized(this) { cache.trim(SPATIAL_CACHE_TILES / 3) }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(
        updated: MutableMapDataWithGeometry = MutableMapDataWithGeometry(),
        deleted: Collection<ElementKey> = emptyList()
    ) {
        if (updated.nodes.isEmpty() && updated.ways.isEmpty() && updated.relations.isEmpty() && deleted.isEmpty()) return

        listeners.forEach { it.onUpdated(updated, deleted) }
    }

    private fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MutableMapDataWithGeometry) {
        listeners.forEach { it.onReplacedForBBox(bbox, mapDataWithGeometry) }
    }

    private fun onCleared() {
        listeners.forEach { it.onCleared() }
    }

    companion object {
        private const val TAG = "MapDataController"
    }
}

// StyleableOverlayManager loads z16 tiles, but we want smaller tiles. Small tiles make db fetches for
// typical getMapDataWithGeometry calls noticeably faster than z16, as they usually only require a small area.
private const val SPATIAL_CACHE_TILE_ZOOM = 17

// Three times the maximum number of tiles that can be loaded at once in StyleableOverlayManager (translated from z16 tiles).
// We don't want to drop tiles from cache already when scrolling the map just a bit, especially
// considering automatic trim may temporarily reduce cache size to 2/3 of maximum.
private const val SPATIAL_CACHE_TILES = 192

// In a city this is roughly the number of nodes in ~20-40 z16 tiles
private const val SPATIAL_CACHE_INITIAL_CAPACITY = 100000
