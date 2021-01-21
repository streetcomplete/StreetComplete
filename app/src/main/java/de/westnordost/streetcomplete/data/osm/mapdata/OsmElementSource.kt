package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element

interface OsmElementSource {

    interface ElementUpdatesListener {
        fun onUpdated(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry)
    }

    fun get(type: Element.Type, id: Long): Element?

    fun addQuestStatusListener(listener: ElementUpdatesListener)
    fun removeQuestStatusListener(listener: ElementUpdatesListener)
}
