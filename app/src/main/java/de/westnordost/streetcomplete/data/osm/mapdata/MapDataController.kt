package de.westnordost.streetcomplete.data.osm.mapdata

import android.util.Log
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.util.SpatialCache
import de.westnordost.streetcomplete.util.ktx.format
import java.lang.System.currentTimeMillis
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
) {

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

    val spatialCache = SpatialCache(
        16,
        SPATIAL_CACHE_SIZE,
        2000,
        { bbox -> getDataInBBoxForSpatialCacheAndPutToNonSpatialCaches(bbox) },
        Node::id, Node::position
    )
    private val wayRelationCache = HashMap<ElementKey, Element>(3000)
    private val wayRelationGeometryCache = HashMap<ElementKey, ElementGeometry>(3000)
    private val wayIdsByNodeIdCache = HashMap<Long, MutableList<Long>>()
    private val relationIdsByElementKeyCache = HashMap<ElementKey, MutableList<Long>>()

    /** update element data because in the given bounding box, fresh data from the OSM API has been
     *  downloaded */
    fun putAllForBBox(bbox: BoundingBox, mapData: MutableMapData) {
        val time = currentTimeMillis()

        val oldElementKeys: Set<ElementKey>
        val geometryEntries: List<ElementGeometryEntry>
        synchronized(this) {
            // for incompletely downloaded relations, complete the map data (as far as possible) with
            // local data, i.e. with local nodes and ways (still) in local storage
            completeMapData(mapData)

            geometryEntries = mapData.mapNotNull { element ->
                val geometry = elementGeometryCreator.create(element, mapData, true)
                geometry?.let { ElementGeometryEntry(element.type, element.id, it) }
            }

            // use cache? but then it's likely the data will be loaded into cache just to be replaced right after
            oldElementKeys = elementDB.getAllKeys(mapData.boundingBox!!).toMutableSet()
            for (element in mapData) {
                oldElementKeys.remove(ElementKey(element.type, element.id))
            }

            deleteFromCache(oldElementKeys)
            spatialCache.replaceAllInBBox(mapData.filterIsInstance<Node>(), bbox) // use bbox, and not of the padded mapData.boundingBox
            addToNonSpatialCaches(mapData, geometryEntries)

            elementDB.deleteAll(oldElementKeys)
            geometryDB.deleteAll(oldElementKeys)
            geometryDB.putAll(geometryEntries)
            elementDB.putAll(mapData)
        }

        Log.i(TAG,
            "Persisted ${geometryEntries.size} and deleted ${oldElementKeys.size} elements and geometries" +
            " in ${((currentTimeMillis() - time) / 1000.0).format(1)}s"
        )

        val mapDataWithGeometry = MutableMapDataWithGeometry(mapData, geometryEntries)
        mapDataWithGeometry.boundingBox = mapData.boundingBox

        onReplacedForBBox(bbox, mapDataWithGeometry)
    }

    fun updateAll(mapDataUpdates: MapDataUpdates) {
        val elements = mapDataUpdates.updated
        // need mapData in order to create (updated) geometry
        val mapData = MutableMapData(elements)

        val deletedKeys: List<ElementKey>
        val geometryEntries: List<ElementGeometryEntry>
        synchronized(this) {
            completeMapData(mapData)

            geometryEntries = elements.mapNotNull { element ->
                val geometry = elementGeometryCreator.create(element, mapData, true)
                geometry?.let { ElementGeometryEntry(element.type, element.id, geometry) }
            }

            val newElementKeys = mapDataUpdates.idUpdates.map { ElementKey(it.elementType, it.newElementId) }
            val oldElementKeys = mapDataUpdates.idUpdates.map { ElementKey(it.elementType, it.oldElementId) }
            deletedKeys = mapDataUpdates.deleted + oldElementKeys

            deleteFromCache(deletedKeys)
            elements.forEach { if (it is Node) spatialCache.putIfTileExists(it) }
            addToNonSpatialCaches(elements, geometryEntries)

            elementDB.deleteAll(deletedKeys)
            geometryDB.deleteAll(deletedKeys)
            geometryDB.putAll(geometryEntries)
            elementDB.putAll(elements)
            createdElementsController.putAll(newElementKeys)
        }

        val mapDataWithGeom = MutableMapDataWithGeometry(mapData, geometryEntries)
        mapDataWithGeom.boundingBox = mapData.boundingBox

        onUpdated(updated = mapDataWithGeom, deleted = deletedKeys)
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

    fun get(type: ElementType, id: Long): Element? {
        val element = if (type == ElementType.NODE) spatialCache.get(id)
            else synchronized(this) { wayRelationCache.getOrPutIfNotNull(ElementKey(type, id)) { elementDB.get(type, id) } }
        return element ?: elementDB.get(type, id)
    }

    fun getGeometry(type: ElementType, id: Long): ElementGeometry? {
        val geometry = if (type == ElementType.NODE) spatialCache.get(id)
                ?.let { ElementPointGeometry(it.position) }
            else synchronized(this) { wayRelationGeometryCache.getOrPutIfNotNull(ElementKey(type, id)) { geometryDB.get(type, id) } }
        return geometry ?: geometryDB.get(type, id)
    }

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> = synchronized(this){
        val geometries = spatialCache.getAll(keys.mapNotNull { if (it.type == ElementType.NODE) it.id else null } )
            .map { it.toElementGeometryEntry() } +
            keys.mapNotNull { key ->
                wayRelationGeometryCache[key]?.let { ElementGeometryEntry(key.type, key.id, it) }
            }
        return if (keys.size == geometries.size) geometries
        else {
            val cachedKeys = geometries.map { ElementKey(it.elementType, it.elementId) }
            val fetchedGeometries = geometries + geometryDB.getAllEntries(keys.filterNot { it in cachedKeys })
            fetchedGeometries.forEach {
                if (it.elementType == ElementType.NODE)
                    wayRelationGeometryCache[ElementKey(it.elementType, it.elementId)] = it.geometry
            }
            fetchedGeometries
        }
    }

    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry {
        val time = currentTimeMillis()
        val nodes = spatialCache.get(bbox)
        val result = MutableMapDataWithGeometry()
        synchronized(this) {
            val wayIds = nodes.mapNotNull { wayIdsByNodeIdCache[it.id] }.flatten().toSet()
            val relationIds = (
                nodes.mapNotNull { relationIdsByElementKeyCache[ElementKey(ElementType.NODE, it.id)] }
                .flatten() +
                wayIds
                    .mapNotNull { relationIdsByElementKeyCache[ElementKey(ElementType.WAY, it)] }
                    .flatten()
                ).toSet()

            val wayAndRelationKeys = wayIds.map { ElementKey(ElementType.WAY, it) } +
                relationIds.map { ElementKey(ElementType.RELATION, it) }
            wayAndRelationKeys.forEach { result.put(wayRelationCache[it]!!, wayRelationGeometryCache[it]) } // any chance the element does not exist? should never happen!
        }

        nodes.forEach { result.put(it, ElementPointGeometry(it.position)) } // create new geometry, as it's not cached
        result.boundingBox = bbox
        Log.i(TAG, "Fetched ${result.size} elements and geometries in ${currentTimeMillis() - time}ms")

        return result
    }

    // here it's necessary that bbox only containing full tiles
    // this is to be called by SpatialCache only, so it should be safe
    private fun getDataInBBoxForSpatialCacheAndPutToNonSpatialCaches(bbox: BoundingBox): List<Node> {
        val time = currentTimeMillis()

        val nodes = nodeDB.getAll(bbox)
        val nodeIds = nodes.map { it.id }
        val ways = wayDB.getAllForNodes(nodeIds)
        val wayIds = ways.map { it.id }
        val relations = relationDB.getAllForElements(nodeIds = nodeIds, wayIds = wayIds)
        val elements = ways + relations + nodes
        val wayAndRelationGeometries = geometryDB.getAllEntries((ways + relations).map { ElementKey(it.type, it.id) })

        // need to forward also nodes to addToNonSpatialCaches because they are not yet cached
        addToNonSpatialCaches(elements, wayAndRelationGeometries) // ca 5-10% of of db operations, no need for improving
        Log.i(TAG, "Fetched ${elements.size} elements and geometries from DB in ${currentTimeMillis() - time}ms")
        return nodes
    }

    private fun Node.toElementGeometryEntry() =
        ElementGeometryEntry(type, id, ElementPointGeometry(position))

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

    fun getNode(id: Long): Node? = spatialCache.get(id) ?: nodeDB.get(id)
    fun getWay(id: Long): Way? = synchronized(this) {
        wayRelationCache.getOrPutIfNotNull(ElementKey(ElementType.WAY, id)) { wayDB.get(id) } as? Way
    }
    fun getRelation(id: Long): Relation? = synchronized(this) {
        wayRelationCache.getOrPutIfNotNull(ElementKey(ElementType.RELATION, id)) { relationDB.get(id) } as? Relation
    }

    fun getAll(elementKeys: Collection<ElementKey>): List<Element> = synchronized(this) {
        val elements = spatialCache.getAll(elementKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null } ) +
            elementKeys.mapNotNull { wayRelationCache[it] }
        return if (elementKeys.size == elements.size) elements
        else {
            val cachedElementKeys = elements.map { ElementKey(it.type, it.id) }
            val fetchedElements = elementDB.getAll(elementKeys.filterNot { it in cachedElementKeys })
            fetchedElements.forEach {
                if (it.type == ElementType.NODE)
                    wayRelationCache[ElementKey(it.type, it.id)] = it
            }
            elements + fetchedElements
        }
    }

    fun getNodes(ids: Collection<Long>): List<Node> {
            val nodes = spatialCache.getAll(ids)
            return if (ids.size == nodes.size) nodes
            else {
                 val cachedNodeIds = nodes.map { it.id }
                nodes + nodeDB.getAll(ids.filterNot { it in cachedNodeIds })
            }
        }
    fun getWays(ids: Collection<Long>): List<Way> = getAll(ids.map { ElementKey(ElementType.WAY, it) }).filterIsInstance<Way>()
    fun getRelations(ids: Collection<Long>): List<Relation> = getAll(ids.map { ElementKey(ElementType.RELATION, it) }).filterIsInstance<Relation>()

    fun getWaysForNode(id: Long): List<Way> = synchronized(this) {
        wayIdsByNodeIdCache.getOrPut(id) {
            val ways = wayDB.getAllForNode(id)
            ways.forEach { wayRelationCache[ElementKey(ElementType.WAY, it.id)] = it }
            ways.map { it.id }.toMutableList()
        }.let { wayIds ->
            wayIds.map { wayRelationCache[ElementKey(ElementType.WAY, it)] as Way }
        }
    }
    fun getRelationsForNode(id: Long): List<Relation> = getRelationsForElement(ElementType.NODE, id)
    fun getRelationsForWay(id: Long): List<Relation> = getRelationsForElement(ElementType.WAY, id)
    fun getRelationsForRelation(id: Long): List<Relation> = getRelationsForElement(ElementType.RELATION, id)
    fun getRelationsForElement(type: ElementType, id: Long): List<Relation> = synchronized(this) {
        relationIdsByElementKeyCache.getOrPut(ElementKey(type, id)) {
            val relations = when (type) {
                ElementType.NODE -> relationDB.getAllForNode(id)
                ElementType.WAY -> relationDB.getAllForWay(id)
                ElementType.RELATION -> relationDB.getAllForRelation(id)
            }
            relations.forEach { wayRelationCache[ElementKey(ElementType.RELATION, it.id)] = it }
            relations.map { it.id }.toMutableList()
        }.let { relationIds ->
            relationIds.map { wayRelationCache[ElementKey(ElementType.RELATION, it)] as Relation }
        }
    }

    fun deleteOlderThan(timestamp: Long, limit: Int? = null): Int {
        val elements: List<ElementKey>
        val elementCount: Int
        val geometryCount: Int
        synchronized(this) {
            elements = elementDB.getIdsOlderThan(timestamp, limit)
            if (elements.isEmpty()) return 0

            deleteFromCache(elements)
            elementCount = elementDB.deleteAll(elements)
            geometryCount = geometryDB.deleteAll(elements)
            createdElementsController.deleteAll(elements)
        }
        Log.i(TAG, "Deleted $elementCount old elements and $geometryCount geometries")

        onUpdated(deleted = elements)

        return elementCount
    }

    fun clear() {
        elementDB.clear()
        geometryDB.clear()
        createdElementsController.clear()
        clearCache()
        onCleared()
    }

    fun clearCache() {
        synchronized(this) {
            wayIdsByNodeIdCache.clear()
            relationIdsByElementKeyCache.clear()
            wayRelationCache.clear()
            wayRelationGeometryCache.clear()
            spatialCache.clear()
        }
    }

    fun trimCache() {
        spatialCache.trim(SPATIAL_CACHE_SIZE / 3)
        trimNonSpatialCaches()
    }

    private fun trimNonSpatialCaches() {
        synchronized(this) {
            val cachedNodeIds = spatialCache.keys
            // ways with at least one node in cache should not be removed
            val waysWithCachedNode = cachedNodeIds.mapNotNull { wayIdsByNodeIdCache[it] }.flatten().toSet()
            val cachedWayAndNodeKeys = cachedNodeIds.map { ElementKey(ElementType.NODE, it) } + waysWithCachedNode.map { ElementKey(ElementType.WAY, it) }
            // relations with at least one element in cache should not be removed
            val relationsWithCachedElement = cachedWayAndNodeKeys.mapNotNull { relationIdsByElementKeyCache[it] }.flatten().toSet()
            wayRelationCache.keys.removeAll {
                if (it.type == ElementType.RELATION)
                    it.id !in relationsWithCachedElement
                else it.id !in waysWithCachedNode
            }
            wayRelationGeometryCache.keys.removeAll {
                if (it.type == ElementType.RELATION)
                    it.id !in relationsWithCachedElement
                else it.id !in waysWithCachedNode
            }

            // now clean up wayIdsByNodeIdCache and relationIdsByElementKeyCache
            wayIdsByNodeIdCache.keys.retainAll { it in cachedNodeIds }
            relationIdsByElementKeyCache.keys.retainAll { it in cachedWayAndNodeKeys }
        }
    }

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

    private fun deleteFromCache(deleted: Collection<ElementKey>) {
        spatialCache.removeAll(deleted.filter { it.type == ElementType.NODE }.map { it.id })
        synchronized(this) {
            deleted.forEach {
                wayRelationCache.remove(it)
                wayRelationGeometryCache.remove(it)
            }
            trimNonSpatialCaches()
        }
    }

    private fun addToNonSpatialCaches(
        elements: Iterable<Element>, // should also contains nodes if they weren't already added to spatialCache
        geometries: Iterable<ElementGeometryEntry> // nodes will be ignored
    ) = synchronized(this) {
        val nodeIds = (elements.filterIsInstance<Node>().map { it.id } + spatialCache.keys).toSet()
        val ways = elements.filterIsInstance<Way>()
        val wayIds = ways.map {it.id}
        val relations = elements.filterIsInstance<Relation>()
        val relationIds = relations.map {it.id}
        (ways + relations).forEach { wayRelationCache[ElementKey(it.type, it.id)] = it }
        geometries.filterNot { it.elementType == ElementType.NODE }
            .forEach { wayRelationGeometryCache[ElementKey(it.elementType, it.elementId)] = it.geometry }

        ways.forEach { way ->
            wayRelationCache[ElementKey(ElementType.WAY, way.id)]?.let { oldWay ->
                // remove old way from wayIdsByNodeIdCache
                (oldWay as Way).nodeIds.forEach {
                    wayIdsByNodeIdCache[it]?.remove(way.id)
                }
            }
            way.nodeIds.forEach {
                if (it in nodeIds)
                    wayIdsByNodeIdCache.getOrPut(it) { ArrayList(2) }.add(way.id)
            }
        }
        relations.forEach { relation ->
            wayRelationCache[ElementKey(ElementType.RELATION, relation.id)]?.let { oldRelation ->
                // remove old relation from relationIdsByElementKeyCache
                (oldRelation as Relation).members.forEach {
                    relationIdsByElementKeyCache[ElementKey(it.type, it.ref)]?.remove(relation.id)
                }
            }
            relation.members.forEach {
                if ((it.ref in nodeIds && it.type == ElementType.NODE)
                    || (it.ref in wayIds && it.type == ElementType.WAY)
                    || (it.ref in relationIds && it.type == ElementType.RELATION))
                    relationIdsByElementKeyCache.getOrPut(ElementKey(it.type, it.ref)) { ArrayList(2) }.add(relation.id)
            }
        }
        // do NOT trim here, as this may be called before nodes are added to spatial cache
        //  specifically in getDataInBBoxForCache
    }

    private fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MutableMapDataWithGeometry) {
        listeners.forEach { it.onReplacedForBBox(bbox, mapDataWithGeometry) }
    }

    private fun onCleared() {
        listeners.forEach { it.onCleared() }
    }

    private fun <K,V> HashMap<K, V>.getOrPutIfNotNull(key: K, valueOrNull: () -> V?): V? {
        val v = get(key)
        if (v == null)
            valueOrNull()?.let {
                put(key, it)
                return it
            }
        return v
    }

    companion object {
        private const val TAG = "MapDataController"
    }
}

private const val SPATIAL_CACHE_SIZE = 32
