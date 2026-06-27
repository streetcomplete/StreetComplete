package de.westnordost.streetcomplete.data.osm.edits

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataRepository
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry

/** Source for map data. It combines the original data downloaded with the edits made.
 *
 *  This class is threadsafe.
 */
interface MapDataWithEditsSource : MapDataRepository {
    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        /** Called when a number of elements have been updated or deleted */
        fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>)

        /** Called when all elements in the given bounding box should be replaced with the elements
         *  in the mapDataWithGeometry */
        fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)

        /** Called when all map data has been cleared */
        fun onCleared()
    }

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    fun getGeometry(type: ElementType, id: Long): ElementGeometry?

    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry>

    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry
}
