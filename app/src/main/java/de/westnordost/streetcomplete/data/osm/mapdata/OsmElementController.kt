package de.westnordost.streetcomplete.data.osm.mapdata

import android.util.Log
import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.*
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryEntry
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller to access element data and its geometry and handle updates to it (from OSM API) */
@Singleton class OsmElementController @Inject internal constructor(
    private val mapDataApi: MapDataApi,
    private val elementDB: MergedElementDao,
    private val geometryDB: ElementGeometryDao,
    private val elementGeometryCreator: ElementGeometryCreator
): OsmElementSource {

    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val elementUpdatesListener: MutableList<OsmElementSource.ElementUpdatesListener> = CopyOnWriteArrayList()

    override fun get(type: Element.Type, id: Long) : Element? = elementDB.get(type, id)

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
    fun updateForBBox(bbox: BoundingBox, mapData: MapData) {
        val time = System.currentTimeMillis()

        val oldElementKeys = geometryDB.getAllKeys(bbox).toMutableSet()
        for (element in mapData) {
            oldElementKeys.remove(ElementKey(element.type, element.id))
        }
        elementDB.deleteAll(oldElementKeys)
        geometryDB.deleteAll(oldElementKeys)
        val geometries = mapData.mapNotNull { element ->
            // TODO hm what about incomplete relations?
            val geometry = elementGeometryCreator.create(element, mapData, true)
            geometry?.let { ElementGeometryEntry(element.type, element.id, it) }
        }
        geometryDB.putAll(geometries)
        elementDB.putAll(mapData)

        val seconds = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Persisted ${geometries.size} elements and geometries in ${seconds}s")

        val mapDataWithGeometry = ImmutableMapDataWithGeometry(mapData, geometries)
        elementUpdatesListener.forEach { it.onUpdateForBBox(bbox, mapDataWithGeometry) }
    }

    /** delete an element because the element does not exist anymore on OSM */
    fun delete(type: Element.Type, id: Long) {
        elementDB.delete(type, id)
        geometryDB.delete(type, id)
        elementUpdatesListener.forEach { it.onDeleted(type, id) }
    }

    /** update an element because the element has changed on OSM */
    fun update(element: Element) {
        val geometry = createGeometry(element) ?: return delete(element.type, element.id)

        geometryDB.put(ElementGeometryEntry(element.type, element.id, geometry))
        elementDB.put(element)
        elementUpdatesListener.forEach { it.onUpdated(element, geometry) }
    }

    private fun createGeometry(element: Element): ElementGeometry? {
        when(element) {
            is Node -> {
                return elementGeometryCreator.create(element)
            }
            is Way -> {
                val mapData: MapData
                try {
                    mapData = mapDataApi.getWayComplete(element.id)
                } catch (e: OsmNotFoundException) {
                    return null
                }
                return elementGeometryCreator.create(element, mapData)
            }
            is Relation -> {
                val mapData: MapData
                try {
                    mapData = mapDataApi.getRelationComplete(element.id)
                } catch (e: OsmNotFoundException) {
                    return null
                }
                return elementGeometryCreator.create(element, mapData)
            }
            else -> return null
        }
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addQuestStatusListener(listener: OsmElementSource.ElementUpdatesListener) {
        elementUpdatesListener.add(listener)
    }
    override fun removeQuestStatusListener(listener: OsmElementSource.ElementUpdatesListener) {
        elementUpdatesListener.remove(listener)
    }

    companion object {
        private const val TAG = "OsmElementDownload"
    }
}
