package de.westnordost.streetcomplete.data.maptiles

import android.content.Context
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MapTilesDownloader(context: Context) {

    data class Tile(val zoom: Int, val x: Int, val y: Int)

    private val offlineManager = OfflineManager.getInstance(context)
    init {
        offlineManager.setOfflineMapboxTileCountLimit(6000) // this is default iirc, todo: test/check what happens when it's exceeded and a download is started
        val ambientCacheSizeInBytes = 50L * 1024 * 1024 // todo: read from settings

        // setMaximumAmbientCacheSize will trim the data if too large, so might take a while.
        //  offline regions count against this size limit, so in worst case maplibre will not cache
        //  any tiles downloaded by panning (offline regions will not get kicked out)
        // TODO: this should be called BEFORE setting a style and loading a map, because otherwise
        //  it will be called with 50 MB at start (means it will possibly delete tiles)
        offlineManager.setMaximumAmbientCacheSize(ambientCacheSizeInBytes, null) // could add a callback
    }
    private val pixelRatio = context.resources.displayMetrics.density
    private val offlineRegionCallback = object : OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
            val time = nowAsEpochMilliseconds()

            offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                override fun mapboxTileCountLimitExceeded(limit: Long) {
                    // limit can be set in offlineManager, default is 6000
                    offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // set to inactive to stop downloading
                    Log.i(TAG, "Tile download limit of $limit exceeded")
                }

                override fun onError(error: OfflineRegionError) {
                    // todo: continue trying, or stop downloading?
                    Log.i(TAG, "Error during download: ${error.reason}, ${error.message}")
                }

                override fun onStatusChanged(status: OfflineRegionStatus) {
                    // status contains information about downloaded tile count and size
                    if (status.isComplete) { // and whether the download is complete
                        offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // this is needed once download is done!
                        // actually we could delete the region now: the tiles stay unless the ambientCacheSize is exceeded (set in OfflineManager)
                        // but maybe the region should be kept for DELETE_OLD_DATA_AFTER, so nothing is deleted
                        // if the region is not deleted, it will stay forever!
                        // offlineRegion.delete(someCallback)
                        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
                        Log.i(TAG, "Downloaded tiles in ${seconds.format(1)}s: ${status.downloadState}, tc ${status.completedTileCount}, ts ${status.completedTileSize}," +
                            "  rc ${status.requiredResourceCount}. rs ${status.completedResourceSize}")
                    } else {
                        Log.i(TAG, "Download status changed, not complete yet: ${status.downloadState}, tc ${status.completedTileCount}, ts ${status.completedTileSize}," +
                            " rc ${status.requiredResourceCount}. rs ${status.completedResourceSize}")
                    }
                    // check whether total tile size and count is returned, or only downloaded ones:
                    //  re-download where dl was already done
                    //   status changes for resources (change count to 278, consecutive changes for updated size)
                    //   then status changes for tiles (increase count by one, increase size, also increase resource size by the tile size)
                    //   no mbgl http requests in between
                    //  download where all tiles exist because of panning
                    //   same resource dl messages, even same size
                    //   then similar map dl messages
                    //   no mbgl http requests in between
                    //  normal dl
                    //   same resource dl messages, even same size
                    //   then similar map dl messages
                    //   mbgl http requests in between map tile dl messages
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
        var tileCount = 0
        var failureCount = 0
        var downloadedSize = 0
        var cachedSize = 0
        val time = nowAsEpochMilliseconds()

        // downloading like https://docs.mapbox.com/android/legacy/maps/examples/set-up-offline-manager/
        // https://maplibre.org/maplibre-native/android/api/-map-libre%20-native%20for%20-android/com.mapbox.mapboxsdk.offline/index.html

        val bounds = LatLngBounds.fromLatLngs(listOf(bbox.max.toLatLng(), bbox.min.toLatLng()))
        val regionDefinition = OfflineTilePyramidRegionDefinition(styleUrl, bounds, 0.0, 16.0, pixelRatio)

        // todo: looks like this is not actually needed??
        //  documentation doesn't explain what this is used for (it's stored in the DB, but...?)
        val metadata = "region_name".toByteArray()

        offlineManager.createOfflineRegion(regionDefinition, metadata, offlineRegionCallback)

        // any way to get failure / success info from maplibre downloader?
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        val failureText = if (failureCount > 0) ". $failureCount tiles failed to download" else ""
        // obviously shows 0...
        Log.i(TAG, "Downloaded $tileCount tiles (${downloadedSize / 1000}kB downloaded, ${cachedSize / 1000}kB already cached) in ${seconds.format(1)}s$failureText")
    }

    companion object {
        private const val TAG = "MapTilesDownload"

        // todo: should be a stripped down json, we only use it to get a tiles URL
        private const val styleUrl = "https://streetcomplete.app/map-jawg/streetcomplete.json"
    }
}

private sealed class DownloadResult
private data class DownloadSuccess(val alreadyCached: Boolean, val size: Int) : DownloadResult()
private object DownloadFailure : DownloadResult()

private fun LatLon.toLatLng() = LatLng(latitude, longitude)
