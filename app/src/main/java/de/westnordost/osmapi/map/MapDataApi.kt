package de.westnordost.osmapi.map

import de.westnordost.osmapi.common.errors.OsmNotFoundException
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.toOsmApiBoundingBox

fun MapDataApi.getMap(bounds: BoundingBox): MapData {
    val result = MutableMapData()
    getMap(bounds.toOsmApiBoundingBox(), result)
    return result
}

fun MapDataApi.getWayComplete(id: Long): MapData? =
    try {
        val result = MutableMapData()
        getWayComplete(id, result)
        result
    } catch (e: OsmNotFoundException) {
        null
    }

fun MapDataApi.getRelationComplete(id: Long): MapData? =
    try {
        val result = MutableMapData()
        getRelationComplete(id, result)
        result
    } catch (e: OsmNotFoundException) {
        null
    }

fun MapDataApi.uploadChanges(changesetId: Long, elements: Collection<Element>): ElementUpdates {
    val handler = UpdatedElementsHandler()
    uploadChanges(changesetId, elements, handler)
    return handler.getElementUpdates(elements)
}
