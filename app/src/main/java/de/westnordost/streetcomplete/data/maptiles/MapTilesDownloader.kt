package de.westnordost.streetcomplete.data.maptiles

import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineRegionError
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.map.VectorTileProvider
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapTilesDownloader(
    private val vectorTileProvider: VectorTileProvider,
) {

    data class Tile(val zoom: Int, val x: Int, val y: Int)

    suspend fun download(bbox: BoundingBox) = withContext(Dispatchers.IO) {
        var tileCount = 0
        var failureCount = 0
        var downloadedSize = 0
        var cachedSize = 0
        val time = nowAsEpochMilliseconds()

        // downloading like https://docs.mapbox.com/android/legacy/maps/examples/set-up-offline-manager/
        val pixelRatio = MainActivity.activity!!.resources.displayMetrics.density

        val styleUrl = "use something that works" // maybe should contain the api key
        /*
           the file should be a minimal version of assets/map_theme/jawg-streets.json, but with
           jawgApiKey replaced by the actual API key
           some stripped down working version (except for the key):
{
  "version": 8,
  "glyphs": "https://api.jawg.io/glyphs/{fontstack}/{range}.pbf",
  "sprite": "https://api.jawg.io/sprites/maki-circle+network+road-shield?access-token=jawgApiKey",
  "sources": {
    "streets-v2": {
      "type": "vector",
      "tiles": [
        "https://tile.jawg.io/streets-v2/{z}/{x}/{y}.pbf?access-token=jawgApiKey"
      ],
      "maxzoom": 16,
      "attribution": "<a href='https://jawg.io?utm_medium=map&utm_source=attribution' title='Tiles Courtesy of Jawg Maps' target='_blank' class='jawg-attrib'>&copy; <b>Jawg</b>Maps</a> | <a href='https://www.openstreetmap.org/copyright' title='OpenStreetMap is open data licensed under ODbL' target='_blank' class='osm-attrib'>&copy; OSM contributors</a>",
      "mapbox_logo": false
    }
  },
  "metadata": {
    "taxonomy:title": "Jawg Streets",
    "taxonomy:groups": [
    ],
    "language": "en"
  },
  "layers": [
    {
      "type": "background",
      "id": "background",
      "paint": {
        "background-color": "#e8e2dc"
      }
    }
  ]
}
            todo: style can be reduced even more, and glyph / sprite urls are probably unnecessary
         */
        // todo: information about behavior
        //  when are tiles downloaded again?
        //   a. if tiles belong to an offline region
        //   b. if tiles don't belong to any region
        //  are old tiles always preferred over showing nothing, if new tiles can't be downloaded?
        //   there is some "expires" time and a "must revalidate" value (boolean?) in the database
        // do that thing in a coroutine with dispatchers.io?
/*        val bounds = LatLngBounds.fromLatLngs(listOf(bbox.max.toLatLng(), bbox.min.toLatLng()))
        val regionDefinition = OfflineTilePyramidRegionDefinition(styleUrl, bounds, 0.0, 16.0, pixelRatio)
        val metadata = "region_name".toByteArray() // looks like this is not actually needed
        val callback = object : OfflineManager.CreateOfflineRegionCallback {
            override fun onCreate(offlineRegion: OfflineRegion) {
                // so we created the region -> now download

                // but first the observer must be created
                offlineRegion.setObserver(object : OfflineRegion.OfflineRegionObserver {
                    override fun mapboxTileCountLimitExceeded(limit: Long) {
                        // limit can be set in offlineManager, default is 6000
                        offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // set to inactive to stop downloading
                    }

                    override fun onError(error: OfflineRegionError) {
                        // continue trying, or stop downloading?
                    }

                    override fun onStatusChanged(status: OfflineRegionStatus) {
                        // status contains information about downloaded tile count and size
                        if (status.isComplete) { // and whether the download is complete
                            offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE) // this is needed once download is done!
                            // actually we could delete the region now: the tiles stay unless the ambientCacheSize is exceeded (set in OfflineManager)
                            // but maybe the region should be kept for DELETE_OLD_DATA_AFTER, so nothing is deleted
                            // if the region is not deleted, it will stay forever!
                            // offlineRegion.delete(someCallback)
                        }
                    }
                })

                // start the download by setting state to active
                // must be set to inactive once download is done -> need that observer above
                offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE)
            }

            override fun onError(error: String) {
                // creating the offline region failed, when can this happen?
            }
        }

        OfflineManager.getInstance(MainActivity.activity!!).createOfflineRegion(regionDefinition, metadata, callback)
*/

        // any way to get failure / success info from maplibre downloader?
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        val failureText = if (failureCount > 0) ". $failureCount tiles failed to download" else ""
        Log.i(TAG, "Downloaded $tileCount tiles (${downloadedSize / 1000}kB downloaded, ${cachedSize / 1000}kB already cached) in ${seconds.format(1)}s$failureText")
    }

    companion object {
        private const val TAG = "MapTilesDownload"
    }
}

private sealed class DownloadResult
private data class DownloadSuccess(val alreadyCached: Boolean, val size: Int) : DownloadResult()
private object DownloadFailure : DownloadResult()

fun LatLon.toLatLng() = LatLng(latitude, longitude)
