package de.westnordost.streetcomplete.data.osm.mapdata

import android.util.Log
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsController
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
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

            oldElementKeys = elementDB.getAllKeys(mapData.boundingBox!!).toMutableSet()
            for (element in mapData) {
                oldElementKeys.remove(ElementKey(element.type, element.id))
            }
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
        val elements = elementDB.getAll(bbox)
        /* performance improvement over geometryDB.getAllEntries(elements): no need to query
           nodeDB twice (once for the element, another time for the geometry */
        val elementGeometries = geometryDB.getAllEntries(
            elements.mapNotNull { if (it !is Node) ElementKey(it.type, it.id) else null }
        ) + elements.mapNotNull { if (it is Node) it.toElementGeometryEntry() else null }
        val result = MutableMapDataWithGeometry(elements, elementGeometries)
        result.boundingBox = bbox
        Log.i(TAG, "Fetched ${elements.size} elements and geometries in ${currentTimeMillis() - time}ms")
        return result
    }

    private fun Node.toElementGeometryEntry() =
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

    override fun getNode(id: Long): Node? = nodeDB.get(id)
    override fun getWay(id: Long): Way? = wayDB.get(id)
    override fun getRelation(id: Long): Relation? = relationDB.get(id)

    fun getAll(elementKeys: Collection<ElementKey>): List<Element> = elementDB.getAll(elementKeys)

    fun getNodes(ids: Collection<Long>): List<Node> = nodeDB.getAll(ids)
    fun getWays(ids: Collection<Long>): List<Way> = wayDB.getAll(ids)
    fun getRelations(ids: Collection<Long>): List<Relation> = relationDB.getAll(ids)

    override fun getWaysForNode(id: Long): List<Way> = wayDB.getAllForNode(id)
    override fun getRelationsForNode(id: Long): List<Relation> = relationDB.getAllForNode(id)
    override fun getRelationsForWay(id: Long): List<Relation> = relationDB.getAllForWay(id)
    override fun getRelationsForRelation(id: Long): List<Relation> = relationDB.getAllForRelation(id)

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
