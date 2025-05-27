package de.westnordost.streetcomplete.data.download.strategy

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** Auto download strategy decides how big of an area to download based on the OSM map data density */
abstract class AVariableRadiusStrategy(
    private val mapDataController: MapDataController,
    private val downloadedTilesSource: DownloadedTilesSource
) : AutoDownloadStrategy {

    protected abstract val maxDownloadAreaInKm2: Double
    protected abstract val desiredScoredMapDataCountInVicinity: Int

    override suspend fun getDownloadBoundingBox(pos: LatLon): BoundingBox? {
        val tileZoom = ApplicationConstants.DOWNLOAD_TILE_ZOOM

        val thisTile = pos.enclosingTilePos(tileZoom)
        val hasMissingDataForThisTile = hasMissingDataFor(thisTile.toTilesRect())

        // if at the location where we are, there is nothing yet, first download the tiniest
        // possible bbox (~ 360x360m) so that we can estimate the map data density
        if (hasMissingDataForThisTile) {
            Log.i(TAG, "Downloading tiny area around user")
            return thisTile.asBoundingBox(tileZoom)
        }

        // otherwise, see if anything is missing in a variable radius, based on map data density
        val density = getScoredMapDataDensityFor(thisTile.asBoundingBox(tileZoom))
        val maxRadius = sqrt(maxDownloadAreaInKm2 * 1000 * 1000 / PI)

        var radius = if (density > 0) sqrt(desiredScoredMapDataCountInVicinity / (PI * density)) else maxRadius

        radius = min(radius, maxRadius)

        val activeBoundingBox = pos.enclosingBoundingBox(radius)
        val tilesRect = activeBoundingBox.enclosingTilesRect(tileZoom)
        if (hasMissingDataFor(tilesRect)) {
            Log.i(TAG,
                "Downloading in radius of ${radius.toInt()} meters around user (${tilesRect.size} tiles)")
            return activeBoundingBox
        }
        Log.i(TAG,
            "All downloaded in radius of ${radius.toInt()} meters around user (${tilesRect.size} tiles)")
        return null
    }

    /** return the map data density in scored map data per m² for this given [boundingBox]*/
    private suspend fun getScoredMapDataDensityFor(boundingBox: BoundingBox): Double {
        val areaInKm = boundingBox.area()
        val elementCounts =
            withContext(Dispatchers.IO) { mapDataController.getElementCounts(boundingBox) }
        /* score element types by assumed size in transmission:
         *
         * An average way has about 9 nodes. Ways in average have about 2.5 tags.
         * Less than 3% of all nodes have any tags. A node without tags has about 4 times the
         * size of a single node-ref in a way. a tag is about 1.5x he size of a single node-ref.
         * A relation has on average 4 tags. A way member has about 2 times the size of a single
         * node-ref. Don't know how many members in average. Let's assume 12. */
        return (elementCounts.nodes + elementCounts.ways * 4 + elementCounts.relations * 8) / areaInKm
    }

    /** return if data in the given tiles rect that hasn't been downloaded yet */
    private suspend fun hasMissingDataFor(tilesRect: TilesRect): Boolean {
        val dataExpirationTime = ApplicationConstants.REFRESH_DATA_AFTER
        val ignoreOlderThan = max(0, nowAsEpochMilliseconds() - dataExpirationTime)
        return withContext(Dispatchers.IO) { !downloadedTilesSource.contains(tilesRect, ignoreOlderThan) }
    }

    companion object {
        private const val TAG = "QuestAutoSyncer"
    }
}
