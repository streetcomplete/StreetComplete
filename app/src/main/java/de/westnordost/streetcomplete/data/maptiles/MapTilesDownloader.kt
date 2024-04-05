package de.westnordost.streetcomplete.data.maptiles

import android.content.Context
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLng
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapTilesDownloader(context: Context) {

    data class Tile(val zoom: Int, val x: Int, val y: Int)

    private val offlineManager = OfflineManager.getInstance(context)
    init {
        // When exceeded mapboxTileCountLimitExceeded is called according to documentation,
        // but even when setting to 10 nothing happened
        offlineManager.setOfflineMapboxTileCountLimit(6000) // 6000 is default
    }
    private val pixelRatio = context.resources.displayMetrics.density
    private val offlineRegionCallback = object : OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
            val time = nowAsEpochMilliseconds()

            offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                override fun mapboxTileCountLimitExceeded(limit: Long) {
                    // is this actually called?
                    offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // stop downloading
                    Log.i(TAG, "Tile download limit of $limit exceeded")
                }

                override fun onError(error: OfflineRegionError) {
                    offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // stop downloading
                    Log.i(TAG, "Error during download: ${error.reason}, ${error.message}")
                }

                override fun onStatusChanged(status: OfflineRegionStatus) {
                    // status contains information about downloaded tile count and size
                    if (status.isComplete) { // and whether the download is complete
                        offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // this is needed once download is done!
                        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
                        Log.i(TAG, "Downloaded ${status.completedTileCount} tiles (${status.completedTileSize / 1000}kB) in ${seconds.format(1)}s")
                        // note that the numbers include tiles that were already on device
                        //  no idea how to check which tiles were really downloaded (other than in android log for maplibre)
                        // status.requiredResourceCount and status.completedResourceSize might be interesting too
                    }
                }
            })

            // start the download by setting state to active
            // state must be set to inactive once download is done -> need that observer above
            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
        }

        override fun onError(error: String) {
            // creating the offline region failed, when can this happen?
        }
    }

    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        val bounds = LatLngBounds.fromLatLngs(listOf(bbox.max.toLatLng(), bbox.min.toLatLng()))
        val regionDefinition = OfflineTilePyramidRegionDefinition(styleUrl, bounds, 0.0, 16.0, pixelRatio)

        // what to use metadata for? in database it's called "description"
        // no need for identifier, region already gets a unique id (in callback)
        val metadata = "yes this is a region".toByteArray()
        offlineManager.createOfflineRegion(regionDefinition, metadata, offlineRegionCallback)
    }

    companion object {
        private const val TAG = "MapTilesDownload"

        // todo: should be a stripped down json, we only use it to get a tiles URL and want to minimize download size
        private const val styleUrl = "https://streetcomplete.app/map-jawg/streetcomplete.json"
    }
}
