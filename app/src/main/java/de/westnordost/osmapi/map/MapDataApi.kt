package de.westnordost.osmapi.map

import de.westnordost.osmapi.common.errors.OsmQueryTooBigException
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
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

fun MapDataApi.getMapAndHandleTooBigQuery(bounds: BoundingBox): MapData {
    val result = MapData()
    try {
        getMap(bounds, result)
    } catch (e : OsmQueryTooBigException) {
        for (subBounds in bounds.splitIntoFour()) {
            result.add(getMap(subBounds))
        }
    }
    return result
}

fun BoundingBox.splitIntoFour(): List<BoundingBox> {
    val center = OsmLatLon((maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2)
    return listOf(
        BoundingBox(minLatitude,     minLongitude,     center.latitude, center.longitude),
        BoundingBox(minLatitude,     center.longitude, center.latitude, maxLongitude),
        BoundingBox(center.latitude, minLongitude,     maxLatitude,     center.longitude),
        BoundingBox(center.latitude, center.longitude, maxLatitude,     maxLongitude)
    )
}