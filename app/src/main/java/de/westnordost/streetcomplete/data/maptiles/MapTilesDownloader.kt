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
    private val pixelRatio = context.resources.displayMetrics.density
    private val offlineRegionCallback = object : OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
            val time = nowAsEpochMilliseconds()

            offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                override fun mapboxTileCountLimitExceeded(limit: Long) {
                    // was never called in a test with 10 tiles limit
                    // ignore since we limit cache only by size
                }

                override fun onError(error: OfflineRegionError) {
                    offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // stop downloading
                    Log.i(TAG, "Error during download: ${error.reason}, ${error.message}")
                }

                override fun onStatusChanged(status: OfflineRegionStatus) {
                    if (status.isComplete) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // also needed when download is complete
                        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
                        Log.i(TAG, "Downloaded ${status.completedTileCount} tiles (${status.completedTileSize / 1000}kB) in ${seconds.format(1)}s")
                        // note that the numbers include tiles that were already on device
                        //  no idea how to check which tiles were really downloaded (other than in android log for MapLibre)
                        // status.requiredResourceCount and status.completedResourceSize might be interesting too
                    }
                }
            })

            // start the download by setting state to active
            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
        }

        override fun onError(error: String) {
            // creating the offline region failed, when can this happen?
        }
    }

    /** delete regions, which allows contained tiles to be deleted if cache size is exceeded */
    fun deleteRegionsOlderThan(olderThan: Long) {
        offlineManager.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback{
            override fun onError(error: String) { }

            override fun onList(offlineRegions: Array<OfflineRegion>?) {
                offlineRegions?.forEach {
                    val timestamp = it.metadata.toString(Charsets.UTF_8).toLongOrNull() ?: 0
                    if (timestamp < olderThan) {
                        it.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
                            override fun onDelete() {}

                            override fun onError(error: String) {}
                        })
                    }
                }
            }
        })
    }

    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        val bounds = LatLngBounds.fromLatLngs(listOf(bbox.max.toLatLng(), bbox.min.toLatLng()))
        val regionDefinition = OfflineTilePyramidRegionDefinition(styleUrl, bounds, 0.0, 16.0, pixelRatio)

        // store timestamp as metadata for deleting areas older than
        // (could also be done directly from the long)
        val metadata = nowAsEpochMilliseconds().toString().toByteArray(Charsets.UTF_8)
        offlineManager.createOfflineRegion(regionDefinition, metadata, offlineRegionCallback)
    }

    companion object {
        private const val TAG = "MapTilesDownload"

        private const val styleUrl = "https://streetcomplete.app/map-jawg/streetcomplete.json"
    }
}
