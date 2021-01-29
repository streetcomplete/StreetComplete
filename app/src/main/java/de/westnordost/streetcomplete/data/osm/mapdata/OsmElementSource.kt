package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element

interface OsmElementSource {

    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        /** Called when a number of elements have been updated or deleted */
        fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>)

        /** Called when all elements in the given bounding box should be replaced with the elements
         *  in the mapDataWithGeometry */
        fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)
    }

    /** get element of the given type and id */
    fun get(type: Element.Type, id: Long): Element?

    /** get all element data with its geometry within the given bounding box */
    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
