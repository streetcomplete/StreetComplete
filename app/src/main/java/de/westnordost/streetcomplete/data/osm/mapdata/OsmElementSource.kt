package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry

interface OsmElementSource {

    /** Interface to be notified of new or updated OSM elements */
    interface Listener {
        fun onUpdated(element: Element, geometry: ElementGeometry)
        fun onDeleted(type: Element.Type, id: Long)
        fun onUpdatedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)
    }

    /** get element of the given type and id */
    fun get(type: Element.Type, id: Long): Element?

    /** get all element data with its geometry within the given bounding box */
    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
