package de.westnordost.osmapi.map

import de.westnordost.osmapi.map.data.BoundingBox

fun MapDataDao.getMap(bounds: BoundingBox): MapData {
    val result = MapData()
    getMap(bounds, result)
    return result
}

fun MapDataDao.getWayComplete(id: Long): MapData {
    val result = MapData()
    getWayComplete(id, result)
    return result
}

fun MapDataDao.getRelationComplete(id: Long): MapData {
    val result = MapData()
    getRelationComplete(id, result)
    return result
}
