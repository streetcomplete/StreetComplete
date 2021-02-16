package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.MapDataApi

fun MapDataApi.getMap(bounds: BoundingBox): MapData {
    val result = MutableMapData()
    getMap(bounds, result)
    return result
}

fun MapDataApi.getWayComplete(id: Long): MapData {
    val result = MutableMapData()
    getWayComplete(id, result)
    return result
}

fun MapDataApi.getRelationComplete(id: Long): MapData {
    val result = MutableMapData()
    getRelationComplete(id, result)
    return result
}

fun MapDataApi.uploadChanges(changesetId: Long, elements: Collection<Element>): ElementUpdates {
    val handler = UpdatedElementsHandler()
    uploadChanges(changesetId, elements, handler)
    return handler.getElementUpdates(elements)
}
