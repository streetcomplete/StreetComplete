package de.westnordost.streetcomplete.data.osm.mapdata

import android.util.Log
import de.westnordost.osmapi.map.*
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.elementgeometry.*
import de.westnordost.streetcomplete.ktx.format
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller to access element data and its geometry and handle updates to it (from OSM API) */
@Singleton class OsmElementController @Inject internal constructor(
    private val elementDB: MergedElementDao,
    private val wayDB: WayDao,
    private val nodeDB: NodeDao,
    private val geometryDB: ElementGeometryDao,
    private val elementGeometryCreator: ElementGeometryCreator
): OsmElementSource {

    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val listener: MutableList<OsmElementSource.Listener> = CopyOnWriteArrayList()

    override fun get(type: Element.Type, id: Long) : Element? = elementDB.get(type, id)

    override fun getGeometry(type: Element.Type, id: Long) : ElementGeometry? = geometryDB.get(type, id)

    override fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry {
        val time = System.currentTimeMillis()
        val elementGeometryEntries = geometryDB.getAllEntries(bbox)
        val elementKeys = elementGeometryEntries.map { ElementKey(it.elementType, it.elementId) }
        val mapData = MutableMapData()
        mapData.addAll(elementDB.getAll(elementKeys))
        mapData.handle(bbox)
        val result = ImmutableMapDataWithGeometry(mapData, elementGeometryEntries)
        Log.i(TAG, "Fetched ${elementKeys.size} elements and geometries in ${System.currentTimeMillis() - time}ms")
        return result
    }

    /** update element data because in the given bounding box, fresh data from the OSM API has been
     *  downloaded */
    fun putAllForBBox(bbox: BoundingBox, mapData: MutableMapData) {
        val time = System.currentTimeMillis()

        // for incompletely downloaded relations, complete the map data (as far as possible) with
        // local data, i.e. with local nodes and ways (still) in local storage
        completeMapData(mapData)

        val oldElementKeys = geometryDB.getAllKeys(mapData.boundingBox!!).toMutableSet()
        for (element in mapData) {
            oldElementKeys.remove(ElementKey(element.type, element.id))
        }
        elementDB.deleteAll(oldElementKeys)
        geometryDB.deleteAll(oldElementKeys)
        val geometries = mapData.mapNotNull { element ->
            val geometry = elementGeometryCreator.create(element, mapData, true)
            geometry?.let { ElementGeometryEntry(element.type, element.id, it) }
        }
        geometryDB.putAll(geometries)
        elementDB.putAll(mapData)

        val seconds = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG,"Persisted ${geometries.size} and deleted ${oldElementKeys.size} elements and geometries in ${seconds.format(1)}s")

        val mapDataWithGeometry = ImmutableMapDataWithGeometry(mapData, geometries)
        onUpdateForBBox(bbox, mapDataWithGeometry)
    }

    /** delete elements because the elements doe not exist anymore on OSM */
    fun deleteAll(elementKeys: Collection<ElementKey>) {
        elementDB.deleteAll(elementKeys)
        geometryDB.deleteAll(elementKeys)
        onUpdated(deleted = elementKeys)
    }

    /** update elements because the elements have changed on OSM */
    fun putAll(elements: Collection<Element>) {
        val mapData = MutableMapData()
        mapData.addAll(elements)
        completeMapData(mapData)

        val elementGeometryEntries = elements.mapNotNull { element ->
            val geometry = elementGeometryCreator.create(element, mapData, true)
            geometry?.let { ElementGeometryEntry(element.type, element.id, geometry) }
        }

        val mapDataWithGeom = ImmutableMapDataWithGeometry(mapData, elementGeometryEntries)

        geometryDB.putAll(elementGeometryEntries)
        elementDB.putAll(elements)

        onUpdated(updated = mapDataWithGeom)
    }

    private fun completeMapData(mapData: MutableMapData) {
        val missingNodeIds = mutableListOf<Long>()
        val missingWayIds = mutableListOf<Long>()
        for (relation in mapData.relations) {
            for (member in relation.members) {
                if (member.type == Element.Type.NODE && mapData.getNode(member.ref) == null) {
                    missingNodeIds.add(member.ref)
                }
                if (member.type == Element.Type.WAY && mapData.getWay(member.ref) == null) {
                    missingWayIds.add(member.ref)
                }
            }
        }
        val ways = wayDB.getAll(missingWayIds)
        for (way in ways) {
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

    fun deleteUnreferencedOlderThan(timestamp: Long) {
        val deletedElements = elementDB.getUnusedAndOldIds(timestamp)
        elementDB.deleteAll(deletedElements)
        val deletedGeometries = geometryDB.deleteAll(deletedElements)
        onUpdated(deleted = deletedElements)
        Log.i(TAG,"Deleted ${deletedElements.size} old elements and $deletedGeometries geometries")
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addListener(listener: OsmElementSource.Listener) {
        this.listener.add(listener)
    }
    override fun removeListener(listener: OsmElementSource.Listener) {
        this.listener.remove(listener)
    }

    private fun onUpdated(updated: MapDataWithGeometry = EmptyMapDataWithGeometry(), deleted: Collection<ElementKey> = emptyList()) {
        listener.forEach { it.onUpdated(updated, deleted) }
    }
    private fun onUpdateForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        listener.forEach { it.onReplacedForBBox(bbox, mapDataWithGeometry) }
    }

    companion object {
        private const val TAG = "OsmElementDownload"
    }
}
