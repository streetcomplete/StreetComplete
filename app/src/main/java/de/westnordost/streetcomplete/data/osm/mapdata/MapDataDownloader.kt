package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/** Takes care of downloading all note and osm quests */
class MapDataDownloader(
    private val mapDataApi: MapDataApiClient,
    private val mapDataController: MapDataController
) {
    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        val time = nowAsEpochMilliseconds()

        val expandedBBox = bbox.enlargedBy(ApplicationConstants.QUEST_FILTER_PADDING)
        val mapData = getMapAndHandleTooBigQuery(expandedBBox)

        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Downloaded ${mapData.nodes.size} nodes, ${mapData.ways.size} ways and ${mapData.relations.size} relations in ${seconds.format(1)}s")

        yield()

        mapDataController.putAllForBBox(bbox, mapData)
    }

    private suspend fun getMapAndHandleTooBigQuery(bounds: BoundingBox): MutableMapData {
        try {
            return mapDataApi.getMap(bounds, ApplicationConstants.IGNORED_RELATION_TYPES)
        } catch (e: QueryTooBigException) {
            val mapData = MutableMapData()
            for (subBounds in bounds.splitIntoFour()) {
                mapData.addAll(getMapAndHandleTooBigQuery(subBounds))
            }
            mapData.boundingBox = bounds
            return mapData
        }
    }

    companion object {
        private const val TAG = "MapDataDownload"
    }
}

private fun BoundingBox.splitIntoFour(): List<BoundingBox> {
    val center = LatLon((max.latitude + min.latitude) / 2, (max.longitude + min.longitude) / 2)
    return listOf(
        BoundingBox(min.latitude,    min.longitude,    center.latitude, center.longitude),
        BoundingBox(min.latitude,    center.longitude, center.latitude, max.longitude),
        BoundingBox(center.latitude, min.longitude,    max.latitude,    center.longitude),
        BoundingBox(center.latitude, center.longitude, max.latitude,    max.longitude)
    )
}
