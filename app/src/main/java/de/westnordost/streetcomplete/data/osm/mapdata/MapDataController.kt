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

    // todo: waste less memory
    //  now for 18k elements: 8.4 MB
    //  HashMap<ElementKey, Pair<Element, ElementGeometry?>> instead of 2 caches: 7.8 MB
    //  have a fixed key for each element (element.key), and make use of it: 8.2 MB
    //  -> with both, almost 10% could be saved

    val spatialCache = SpatialCache(
        16,
        SPATIAL_CACHE_SIZE,
        2000,
        { bbox -> getDataInBBoxForCache(bbox) },
        Node::id, Node::position
    )
    // cache by ElementKey, or multiple caches by id?
    //  -> performance for getMapDataWithGeometry is always a little better for caches by ElementKey
    //   no further performance differences found, so use the cache by ElementKey
    // TODO: use geometries instead of entries?
    private val wrCache = HashMap<ElementKey, Element>(3000) // 20000 elements is roughly 4-6 z16 tiles in a city, but most of it is nodes
    private val wrGeometryCache = HashMap<ElementKey, ElementGeometryEntry>(3000)
    private val wayIdsByNodeIdCache = HashMap<Long, MutableList<Long>>() // <NodeId, <List<WayId>>
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
            elements.forEach { if (it is Node) spatialCache.putIfTileExists(it) } // todo: mass-put if this is slow
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
            else synchronized(this) { wrCache[ElementKey(type, id)] }
        return element ?: elementDB.get(type, id)
    }

    fun getGeometry(type: ElementType, id: Long): ElementGeometry? {
        val geometry = if (type == ElementType.NODE) spatialCache.get(id)
                ?.let { ElementPointGeometry(it.position) }
            else synchronized(this) { wrGeometryCache[ElementKey(type, id)]?.geometry }
        return geometry ?: geometryDB.get(type, id)
    }

    // todo: if we use wrCache = HashMap<ElementKey, Pair<Element, ElementGeometry?>>, we
    //  could avoid db queries if a geometry is null (plus it's a little bit smaller in memory)
    // but on the other hand, if we don't, we could add results from db to cache, which would avoid
    //  duplicate geometry queries (first when loading quests, then later when loading map data)
    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> {
        val geometries = spatialCache.getAll(keys.mapNotNull { if (it.type == ElementType.NODE) it.id else null } )
            .map { it.toElementGeometryEntry() } +
            synchronized(this) { keys.mapNotNull { wrGeometryCache[it] } }
        return if (keys.size == geometries.size) geometries
        else {
            val cachedKeys = geometries.map { ElementKey(it.elementType, it.elementId) }
            geometries + geometryDB.getAllEntries(keys.filterNot { it in cachedKeys })
        }
    }

    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry {
        val time = currentTimeMillis()
        val wr: Collection<Element>
        val wrGeometries: Collection<ElementGeometryEntry>
        val nodes = spatialCache.get(bbox)
        synchronized(this) { // this part is the slowest in here, but still ok
            val wayIds = nodes.mapNotNull { wayIdsByNodeIdCache[it.id] }.flatten().toSet()
            val relationIds = (
                nodes.mapNotNull { relationIdsByElementKeyCache[ElementKey(ElementType.NODE, it.id)] }
                .flatten() +
                wayIds
                    .mapNotNull { relationIdsByElementKeyCache[ElementKey(ElementType.WAY, it)] }
                    .flatten()
                ).toSet()

            val wrKeys = wayIds.map { ElementKey(ElementType.WAY, it) } +
                relationIds.map { ElementKey(ElementType.RELATION, it) }
            wr = wrKeys.map { wrCache[it]!! } // wrCache should contain all ways/relations for all nodes we have in spatialCache, but maybe still use the safe version getAll(wrKeys)?
            wrGeometries = wrKeys.mapNotNull { wrGeometryCache[it] }
        }

        val result = MutableMapDataWithGeometry(wr, wrGeometries)
        // todo: always create new node geometry, or store and re-use?
        //  storing wastes memory (a lot, due to the probably involved HashMap and the number of nodes), creating is not nice to GC
        nodes.forEach { result.put(it, ElementPointGeometry(it.position)) }
        result.boundingBox = bbox
        Log.i(TAG, "Fetched ${result.size} elements and geometries in ${currentTimeMillis() - time}ms")

        // trimNonSpatialCaches now?
        return result
    }

    // here it's necessary that bbox only containing full tiles
    // this is to be called by SpatialCache only, so it should be safe
    private fun getDataInBBoxForCache(bbox: BoundingBox): List<Node> {
        val time = currentTimeMillis()

        val nodes = nodeDB.getAll(bbox)
        val nodeIds = nodes.map { it.id }
        val ways = wayDB.getAllForNodes(nodeIds)
        val wayIds = ways.map { it.id }
        val relations = relationDB.getAllForElements(nodeIds = nodeIds, wayIds = wayIds)
        val wr = ways + relations
        val elements = wr + nodes
        val wrGeometries = geometryDB.getAllEntries(wr.map { ElementKey(it.type, it.id) })

        // noo need to create node geometries here, this is done later (currently...)
        //  they are ignored in addToNonSpatialCaches anyway
        addToNonSpatialCaches(elements, wrGeometries) // ca 5-10% of time spent -> fast enough? I think so
        Log.i(TAG, "Fetched ${elements.size} elements and geometries from DB in ${currentTimeMillis() - time}ms")
        return nodes
    }

    private fun Node.toElementGeometryEntry() =
        ElementGeometryEntry(type, id, ElementPointGeometry(position))

    data class ElementCounts(val nodes: Int, val ways: Int, val relations: Int)
    // this is used after downloading one tile with auto-download, so we should have it in cache
    fun getElementCounts(bbox: BoundingBox): ElementCounts {
        val data = getMapDataWithGeometry(bbox)
        return ElementCounts(
            data.count { it is Node },
            data.count { it is Way },
            data.count { it is Relation }
        )
    }

    fun getNode(id: Long): Node? = spatialCache.get(id) ?: nodeDB.get(id)
    fun getWay(id: Long): Way? = synchronized(this) { wrCache[ElementKey(ElementType.WAY, id)] as? Way } ?: wayDB.get(id)
    fun getRelation(id: Long): Relation? = synchronized(this) { wrCache[ElementKey(ElementType.RELATION, id)] as? Relation } ?: relationDB.get(id)

    fun getAll(elementKeys: Collection<ElementKey>): List<Element> {
        val elements = spatialCache.getAll(elementKeys.mapNotNull { if (it.type == ElementType.NODE) it.id else null } ) +
            synchronized(this) { elementKeys.mapNotNull { wrCache[it] } }
        return if (elementKeys.size == elements.size) elements
        else {
            val cachedElementKeys = elements.map { ElementKey(it.type, it.id) }
            elements + elementDB.getAll(elementKeys.filterNot { it in cachedElementKeys })
        }
    }

    // todo: just move it to getAll(ids.map { ElementKey(type, it) }).filterIsInstance<type>()?
    fun getNodes(ids: Collection<Long>): List<Node> {
            val nodes = spatialCache.getAll(ids)
            return if (ids.size == nodes.size) nodes
            else {
                 val cachedNodeIds = nodes.map { it.id }
                nodes + nodeDB.getAll(ids.filterNot { it in cachedNodeIds })
            }
        }
    fun getWays(ids: Collection<Long>): List<Way> {
        val ways = synchronized(this) { ids.mapNotNull { wrCache[ElementKey(ElementType.WAY, it)] as? Way } }
        return if (ids.size == ways.size) ways
        else {
            val cachedWayIds = ways.map { it.id }
            ways + wayDB.getAll(ids.filterNot { it in cachedWayIds })
        }
    }
    fun getRelations(ids: Collection<Long>): List<Relation>  {
        val relations = synchronized(this) { ids.mapNotNull { wrCache[ElementKey(ElementType.RELATION, it)] as? Relation } }
        return if (ids.size == relations.size) relations
        else {
            val cachedRelationIds = relations.map { it.id }
            relations + relationDB.getAll(ids.filterNot { it in cachedRelationIds })
        }
    }

    fun getWaysForNode(id: Long): List<Way> = synchronized(this) {
        wayIdsByNodeIdCache[id]?.let { wayIds ->
            wayIds.map { wrCache[ElementKey(ElementType.WAY, it)] as Way }
        }
    } ?: wayDB.getAllForNode(id)
    fun getRelationsForNode(id: Long): List<Relation> = synchronized(this) {
        relationIdsByElementKeyCache[ElementKey(ElementType.NODE, id)]?.let { relationIds ->
            relationIds.map { wrCache[ElementKey(ElementType.RELATION, it)] as Relation }
        }
    } ?: relationDB.getAllForNode(id)
    fun getRelationsForWay(id: Long): List<Relation> = synchronized(this) {
        relationIdsByElementKeyCache[ElementKey(ElementType.WAY, id)]?.let { relationIds ->
            relationIds.map { wrCache[ElementKey(ElementType.RELATION, it)] as Relation }
        }
    } ?: relationDB.getAllForWay(id)
    fun getRelationsForRelation(id: Long): List<Relation> = synchronized(this) {
        relationIdsByElementKeyCache[ElementKey(ElementType.RELATION, id)]?.let { relationIds ->
            relationIds.map { wrCache[ElementKey(ElementType.RELATION, it)] as Relation }
        }
    } ?: relationDB.getAllForRelation(id)

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
            wrCache.clear()
            wrGeometryCache.clear()
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
            val waysWithCachedNode = cachedNodeIds.mapNotNull { wayIdsByNodeIdCache[it] }.flatten().toSet() // ways with at least one node in cache
            wrCache.keys.removeAll { it.type == ElementType.WAY && it.id !in waysWithCachedNode } // need to remove now for relation removal to work better (still not perfect)
            wrGeometryCache.keys.removeAll { it.type == ElementType.WAY && it.id !in waysWithCachedNode }
            val cachedWaysAndNodes = cachedNodeIds.map { ElementKey(ElementType.NODE, it) } + waysWithCachedNode.map { ElementKey(ElementType.WAY, it) }
            val relationsWithCachedElement = cachedWaysAndNodes.mapNotNull { relationIdsByElementKeyCache[it] }.flatten().toSet()
            wrCache.keys.removeAll { it.type == ElementType.RELATION && it.id !in relationsWithCachedElement }
            wrGeometryCache.keys.removeAll { it.type == ElementType.RELATION && it.id !in relationsWithCachedElement }
            // now clean up wayIdsByNodeIdCache and relationIdsByElementKeyCache
            wayIdsByNodeIdCache.keys.retainAll { it in cachedNodeIds }
            relationIdsByElementKeyCache.keys.retainAll { it in cachedWaysAndNodes }
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
                wrCache.remove(it)
                wrGeometryCache.remove(it)
            }
            trimNonSpatialCaches()
        }
    }

    private fun addToNonSpatialCaches(
        elements: Iterable<Element>,
        geometries: Iterable<ElementGeometryEntry>
    ) = synchronized(this) {
        val ways = elements.filterIsInstance<Way>()
        val relations = elements.filterIsInstance<Relation>()
        val nodeIds = (elements.filterIsInstance<Node>().map { it.id } + spatialCache.keys).toSet()
        val wayIds = ways.map {it.id}
        val relationIds = relations.map {it.id}
        val wr = ways + relations
        val wrGeometries = geometries.filterNot { it.elementType == ElementType.NODE }
        wr.forEach { wrCache[ElementKey(it.type, it.id)] = it }
        wrGeometries.forEach { wrGeometryCache[ElementKey(it.elementType, it.elementId)] = it }

        ways.forEach { way ->
            wrCache[ElementKey(ElementType.WAY, way.id)]?.let { oldWay ->
                if (oldWay != way)
                    // remove nodes of old way
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
            wrCache[ElementKey(ElementType.RELATION, relation.id)]?.let { oldRelation ->
                if (oldRelation != relation)
                    // remove elements of old relation
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

    companion object {
        private const val TAG = "MapDataController"
    }
}

private const val SPATIAL_CACHE_SIZE = 32
