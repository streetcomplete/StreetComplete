package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry

interface OsmElementSource {

    interface Listener {
        fun onUpdated(element: Element, geometry: ElementGeometry)
        fun onDeleted(type: Element.Type, id: Long)
        fun onUpdatedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)
    }

    fun get(type: Element.Type, id: Long): Element?
    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
