package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry

interface MapDataWithGeometry : MapData {
    fun getNodeGeometry(id: Long): ElementPointGeometry?
    fun getWayGeometry(id: Long): ElementGeometry?
    fun getRelationGeometry(id: Long): ElementGeometry?

    fun getGeometry(elementType: ElementType, id: Long): ElementGeometry? = when(elementType) {
        ElementType.NODE -> getNodeGeometry(id)
        ElementType.WAY -> getWayGeometry(id)
        ElementType.RELATION -> getRelationGeometry(id)
    }
}
