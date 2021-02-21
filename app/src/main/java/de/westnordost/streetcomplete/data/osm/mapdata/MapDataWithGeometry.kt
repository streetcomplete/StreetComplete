package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.MapData
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry

interface MapDataWithGeometry : MapData {
    fun getNodeGeometry(id: Long): ElementPointGeometry?
    fun getWayGeometry(id: Long): ElementGeometry?
    fun getRelationGeometry(id: Long): ElementGeometry?

    fun getGeometry(elementType: Element.Type, id: Long): ElementGeometry? = when(elementType) {
        Element.Type.NODE -> getNodeGeometry(id)
        Element.Type.WAY -> getWayGeometry(id)
        Element.Type.RELATION -> getRelationGeometry(id)
    }
}
