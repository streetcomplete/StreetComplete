package de.westnordost.streetcomplete.data.osm.mapdata

import android.util.Log
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.SpatialCache
import de.westnordost.streetcomplete.util.ktx.containsAny
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
        80, // TODO: 192 is likely WAY too much for S4 mini, maybe make it depend on available memory?
        16,
        { bbox -> getDataInBBoxForCache(bbox) },
        { ids -> removeCachedElementsForNodes(ids) }
    )
    // cache by ElementKey, or multiple caches by id?
    //  -> performance for getMapDataWithGeometry is always a little better for caches by ElementKey
    //   no further performance differences found, so use the cache by ElementKey
    // TODO: use geometries instead of entries? but for creating MutableMapDataWithGeometry the geometryEntries are necessary
    private val elementCache = HashMap<ElementKey, Element>(20000)
    private val geometryCache = HashMap<ElementKey, ElementGeometryEntry>(20000)
    private val wayIdsByNodeIdCache = HashMap<Long, HashSet<Long>>() // <NodeId, <Set<WayId>>
    private val relationIdsByElementKeyCache = HashMap<ElementKey, HashSet<Long>>()

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

            oldElementKeys = elementDB.getAllKeys(mapData.boundingBox!!).toMutableSet() // todo: use cache?
            for (element in mapData) {
                oldElementKeys.remove(ElementKey(element.type, element.id))
            }

            deleteFromCache(oldElementKeys)
            // bbox is the bbox from download, and should be from a z16 TilesRect
            // mapData.boundingBox is expanded by ApplicationConstants.QUEST_FILTER_PADDING (20m)
            // -> use bbox, not mapData.boundingBox
            addToCache(mapData, geometryEntries, bbox)

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
            addToCache(elements, geometryEntries)

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

        val ways = wayDB.getAll(missingWayIds)
        for (way in mapData.ways + ways) {
            for (nodeId in way.nodeIds) {
                if (mapData.getNode(nodeId) == null) {
                    missingNodeIds.add(nodeId)
                }
            }
        }
        val nodes = nodeDB.getAll(missingNodeIds)

        mapData.addAll(nodes)
        mapData.addAll(ways)
    }

    fun get(type: ElementType, id: Long): Element? =
        elementDB.get(type, id)

    fun getGeometry(type: ElementType, id: Long): ElementGeometry? =
        geometryDB.get(type, id)

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> =
        geometryDB.getAllEntries(keys)

    fun getMapDataWithGeometry(bbox: BoundingBox): MutableMapDataWithGeometry {
        val time = currentTimeMillis()
        val nodeIds = spatialCache.get(bbox)
        val wayIds = nodeIds.mapNotNull { wayIdsByNodeIdCache[it] }.flatten().toSet()
        val relationIds = nodeIds.mapNotNull { relationIdsByElementKeyCache[ElementKey(ElementType.NODE, it)] }.flatten() +
            wayIds.mapNotNull { relationIdsByElementKeyCache[ElementKey(ElementType.WAY, it)] }.flatten() // how fast?

        // interestingly, here 1 cache is clearly faster than 3 separate caches for node/way/relation, while everywhere else it's the same
        val elementKeys = (nodeIds.map { ElementKey(ElementType.NODE, it) } + wayIds.map { ElementKey(ElementType.WAY, it) } + relationIds.map { ElementKey(ElementType.RELATION, it) }).toSet()
        val elements = elementCache.filterKeys { it in elementKeys }.values
        val elementGeometries = geometryCache.filterKeys { it in elementKeys }.values

        val result = MutableMapDataWithGeometry(elements, elementGeometries)
        result.boundingBox = bbox
        Log.i(TAG, "Fetched ${elements.size} elements and geometries in ${currentTimeMillis() - time}ms")
        return result
    }

    // here it's necessary that bbox only containing full tiles
    // this is done by cache, so it should be safe
    private fun getDataInBBoxForCache(bbox: BoundingBox): List<Pair<Long, LatLon>> {
        val time = currentTimeMillis()

        val nodes = nodeDB.getAll(bbox)
        val nodeIds = nodes.map { it.id }
        val ways = wayDB.getAllForNodes(nodeIds)
        val wayIds = ways.map { it.id }
        val relations = relationDB.getAllForElements(nodeIds = nodeIds, wayIds = wayIds)
        val elements = nodes + ways + relations
        val elementGeometries = geometryDB.getAllEntries(
            elements.mapNotNull { if (it !is Node) ElementKey(it.type, it.id) else null }
        ) + elements.mapNotNull { if (it is Node) it.toElementGeometryEntry() else null }

        // this is essentially the same as addToCache, but without putting nodes to spatialCache
        //  and maybe a bit faster because no filtering by element type is necessary
        elementCache.putAll(elements.associateBy { ElementKey(it.type, it.id) })
        geometryCache.putAll(elementGeometries.associateBy { ElementKey(it.elementType, it.elementId) })

        ways.forEach { way ->
            way.nodeIds.forEach { wayIdsByNodeIdCache.getOrPut(it) { hashSetOf() }.add(way.id) }
        }
        relations.forEach { relation ->
            relation.members.forEach { relationIdsByElementKeyCache.getOrPut(ElementKey(it.type, it.ref)) { hashSetOf() }.add(relation.id) }
        }

        Log.i(TAG, "Fetched ${elements.size} elements and geometries from DB in ${currentTimeMillis() - time}ms")
        return nodes.map { it.id to it.position }
    }

    fun Node.toElementGeometryEntry() =
        ElementGeometryEntry(type, id, ElementPointGeometry(position))

    data class ElementCounts(val nodes: Int, val ways: Int, val relations: Int)
    fun getElementCounts(bbox: BoundingBox): ElementCounts {
        val keys = elementDB.getAllKeys(bbox)
        return ElementCounts(
            keys.count { it.type == ElementType.NODE },
            keys.count { it.type == ElementType.WAY },
            keys.count { it.type == ElementType.RELATION }
        )
    }

    fun getNode(id: Long): Node? = nodeDB.get(id)
    fun getWay(id: Long): Way? = wayDB.get(id)
    fun getRelation(id: Long): Relation? = relationDB.get(id)

    fun getAll(elementKeys: Collection<ElementKey>): List<Element> = elementDB.getAll(elementKeys)

    fun getNodes(ids: Collection<Long>): List<Node> = nodeDB.getAll(ids)
    fun getWays(ids: Collection<Long>): List<Way> = wayDB.getAll(ids)
    fun getRelations(ids: Collection<Long>): List<Relation> = relationDB.getAll(ids)

    fun getWaysForNode(id: Long): List<Way> = wayDB.getAllForNode(id)
    fun getRelationsForNode(id: Long): List<Relation> = relationDB.getAllForNode(id)
    fun getRelationsForWay(id: Long): List<Relation> = relationDB.getAllForWay(id)
    fun getRelationsForRelation(id: Long): List<Relation> = relationDB.getAllForRelation(id)

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
        spatialCache.clear()
        wayIdsByNodeIdCache.clear()
        relationIdsByElementKeyCache.clear()
        elementCache.clear()
        geometryCache.clear()
        onCleared()
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
        // this will call removeCachedElementsForNodes
        spatialCache.removeAll(deleted.filter { it.type == ElementType.NODE }.map { it.id })
        // still need to remove elements and geometries in case no nodes were deleted
        deleted.forEach {
            elementCache.remove(it)
            geometryCache.remove(it)
        }
    }

    private fun addToCache(
        elements: Iterable<Element>,
        geometries: Iterable<ElementGeometryEntry>,
        bbox: BoundingBox? = null
    ) {
        val ignoredNodeIds = if (bbox == null) {
            elements.filterIsInstance(Node::class.java).mapNotNull {
                if (spatialCache.putIfTileExists(it.id, it.position))
                    null
                else
                    it.id
            }
        } else {
            spatialCache.replaceAllInBBox(
                elements.filterIsInstance(Node::class.java).map { it.id to it.position },
                bbox
            )
        }

        // don't put ways and relations that only use ignored nodes
        val nodes = elements.filter { it is Node && it.id !in ignoredNodeIds }
        val nodeIds = nodes.map { it.id }
        val ways = elements.filterIsInstance(Way::class.java).filter { it.nodeIds.containsAny(nodeIds) }
        val wayIds = ways.map { it.id }
        val relations = elements.filterIsInstance(Relation::class.java).filter { element ->
            element.members.any {
                (it.type == ElementType.NODE && it.ref in nodeIds) || (it.type == ElementType.WAY && it.ref in wayIds)
            }
        }
        val relationIds = relations.map { it.id }
        elementCache.putAll((nodes+ways+relations).associateBy { ElementKey(it.type, it.id) })
        geometryCache.putAll(geometries
            .filter { (it.elementType == ElementType.NODE && it.elementId in nodeIds) ||
                (it.elementType == ElementType.WAY && it.elementId in wayIds) ||
                (it.elementType == ElementType.RELATION && it.elementId in relationIds)
            }
            .associateBy { ElementKey(it.elementType, it.elementId) }
        )

        ways.forEach { way ->
            way.nodeIds.forEach { wayIdsByNodeIdCache.getOrPut(it) { hashSetOf() }.add(way.id) }
        }
        relations.forEach { relation ->
            relation.members.forEach {
                relationIdsByElementKeyCache.getOrPut(ElementKey(it.type, it.ref)) { hashSetOf() }.add(relation.id)
            }
        }

    }

    private fun removeCachedElementsForNodes(nodesToRemove: Collection<Long>) {
        val waysToRemove = hashSetOf<Long>()
        wayIdsByNodeIdCache.filterKeys { it in nodesToRemove }.values.forEach {
            waysToRemove.addAll(it)
        }
        // don't remove way if all some cached nodes are not on the to-remove list
        waysToRemove.retainAll { wayId ->
            val way = elementCache[ElementKey(ElementType.WAY, wayId)]!! as Way
            val cachedNodesOfWay = way.nodeIds.filter { elementCache.containsKey(ElementKey(ElementType.NODE, it)) }
            nodesToRemove.containsAll(cachedNodesOfWay)
        }

        val relationsToRemove = hashSetOf<Long>()
        relationIdsByElementKeyCache.filterKeys {
            (it.type == ElementType.NODE && it.id in nodesToRemove) || (it.type == ElementType.WAY && it.id in waysToRemove)
        }.values.forEach { relationsToRemove.addAll(it) }
        relationsToRemove.retainAll { relationId ->
            val relation = elementCache[ElementKey(ElementType.RELATION, relationId)]!! as Relation
            val cachedNodesOfRelation = relation.members.filter { it.type == ElementType.NODE && elementCache.containsKey(ElementKey(ElementType.NODE, it.ref)) }.map { it.ref }
            val cachedWaysOfRelation = relation.members.filter { it.type == ElementType.WAY && elementCache.containsKey(ElementKey(ElementType.WAY, it.ref)) }.map { it.ref }
            waysToRemove.containsAll(cachedWaysOfRelation) && nodesToRemove.containsAll(cachedNodesOfRelation)
        }

        val keysToRemove = nodesToRemove.map { ElementKey(ElementType.NODE, it) } + waysToRemove.map { ElementKey(ElementType.WAY, it) } + relationsToRemove.map { ElementKey(ElementType.RELATION, it) }
        // interestingly keys.removeAll is slow compared to the loop, even when using a set
        keysToRemove.forEach {
            elementCache.remove(it)
            geometryCache.remove(it)
            relationIdsByElementKeyCache.remove(it)
        }
        wayIdsByNodeIdCache.keys.removeAll(nodesToRemove) // is toSet() really faster? no duplicates anyway
        // TODO: cleanup is incomplete! on trim to 0 tiles wayIdsByNodeIdCache and relationIdsByElementKeyCache are still not empty
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
