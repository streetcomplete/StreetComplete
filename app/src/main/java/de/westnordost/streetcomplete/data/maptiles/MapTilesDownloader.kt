package de.westnordost.streetcomplete.data.maptiles

import android.content.Context
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitCreateOfflineRegion
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitDelete
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitDownload
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitGetOfflineRegions
import de.westnordost.streetcomplete.screens.main.map.maplibre.awaitResetDatabase
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLng
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre

class MapTilesDownloader(private val context: Context) {

    init {
        try {
            // must be called before getting OfflineManager instance but will throw an exception if
            // not called from the main thread
            MapLibre.getInstance(context)
        } catch (_: Exception) { }
    }

    suspend fun clear() {
        try {
            OfflineManager.getInstance(context).awaitResetDatabase()
        } catch (e: Exception) {
            Log.w(TAG, e.message.orEmpty(), e)
        }
    }

    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        val bounds = LatLngBounds.fromLatLngs(listOf(bbox.max.toLatLng(), bbox.min.toLatLng()))
        val pixelRatio = context.resources.displayMetrics.density
        val regionDefinition = OfflineTilePyramidRegionDefinition(styleUrl, bounds, 0.0, 16.0, pixelRatio)

        // store timestamp as metadata for deleting areas older than
        // (could also be done directly from the long)
        val metadata = nowAsEpochMilliseconds().toString().toByteArray(Charsets.UTF_8)
        try {
            val offlineRegion = OfflineManager.getInstance(context).awaitCreateOfflineRegion(regionDefinition, metadata)
            val time = nowAsEpochMilliseconds()
            val status = offlineRegion.awaitDownload()
            val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
            Log.i(TAG, "Downloaded ${status.completedTileCount} tiles (${status.completedTileSize / 1000}kB) in ${seconds.format(1)}s")
            // note that the numbers include tiles that were already on device
            //  no idea how to check which tiles were really downloaded (other than in android log for MapLibre)
            // status.requiredResourceCount and status.completedResourceSize might be interesting too
        } catch (e: Exception) {
            Log.w(TAG, e.message.orEmpty(), e)
        }
    }

    companion object {
        private const val TAG = "MapTilesDownload"

        private const val styleUrl = "https://streetcomplete.app/map-jawg/streetcomplete.json"
    }
}
