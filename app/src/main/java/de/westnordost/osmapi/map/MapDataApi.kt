package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.MapDataApi

fun MapDataApi.getMap(bounds: BoundingBox): MapData {
    val result = MapData()
    getMap(bounds, result)
    return result
}

fun MapDataApi.getWayComplete(id: Long): MapData {
    val result = MapData()
    getWayComplete(id, result)
    return result
}

fun MapDataApi.getRelationComplete(id: Long): MapData {
    val result = MapData()
    getRelationComplete(id, result)
    return result
}
