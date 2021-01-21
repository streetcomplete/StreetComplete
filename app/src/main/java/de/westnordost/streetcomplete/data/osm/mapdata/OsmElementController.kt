package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryEntry
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller to access element data and its geometry and handle updates to it (from OSM API) */
@Singleton class OsmElementController @Inject internal constructor(
    private val elementDao: MergedElementDao,
    private val geometryDao: ElementGeometryDao,
    private val elementGeometryCreator: ElementGeometryCreator
): OsmElementSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val elementUpdatesListener: MutableList<OsmElementSource.ElementUpdatesListener> = CopyOnWriteArrayList()

    override fun get(type: Element.Type, id: Long) : Element? = elementDao.get(type, id)

    fun replaceInBBox(bbox: BoundingBox, mapData: MapData) {

        val oldElementKeys = geometryDao.getAllKeys(bbox).toMutableSet()
        for (element in mapData) {
            oldElementKeys.remove(ElementKey(element.type, element.id))
        }
        geometryDao.deleteAll(oldElementKeys)
        elementDao.deleteAll(oldElementKeys)
        val geometries = mapData.mapNotNull { element ->
            val geometry = elementGeometryCreator.create(element, mapData, true)
            geometry?.let { ElementGeometryEntry(element.type, element.id, it) }
        }
        geometryDao.putAll(geometries)
        elementDao.putAll(mapData)

        val mapDataWithGeometry = ImmutableMapDataWithGeometry(mapData, geometries)
        elementUpdatesListener.forEach { it.onUpdated(bbox, mapDataWithGeometry) }
    }

    /* ------------------------------------ Listeners ------------------------------------------- */

    override fun addQuestStatusListener(listener: OsmElementSource.ElementUpdatesListener) {
        elementUpdatesListener.add(listener)
    }
    override fun removeQuestStatusListener(listener: OsmElementSource.ElementUpdatesListener) {
        elementUpdatesListener.remove(listener)
    }
}
