package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry

interface OsmElementSource {

    interface ElementUpdatesListener {
        fun onUpdateForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)
        fun onDeleted(type: Element.Type, id: Long)
        fun onUpdated(element: Element, geometry: ElementGeometry)
    }

    fun get(type: Element.Type, id: Long): Element?
    fun getMapDataWithGeometry(bbox: BoundingBox): MapDataWithGeometry

    fun addElementUpdatesListener(listener: ElementUpdatesListener)
    fun removeElementUpdatesListener(listener: ElementUpdatesListener)
}
