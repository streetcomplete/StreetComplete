package de.westnordost.streetcomplete.data.download

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesType
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataDownloader
import de.westnordost.streetcomplete.data.osmnotes.NotesDownloader
import de.westnordost.streetcomplete.ktx.format
import de.westnordost.streetcomplete.util.TilesRect
import kotlinx.coroutines.*
import java.lang.System.currentTimeMillis
import javax.inject.Inject
import kotlin.math.max

/** Downloads all the things */
class Downloader @Inject constructor(
    private val notesDownloader: NotesDownloader,
    private val mapDataDownloader: MapDataDownloader,
    private val mapTilesDownloader: MapTilesDownloader,
    private val downloadedTilesDb: DownloadedTilesDao
) {
    suspend fun download(tiles: TilesRect, ignoreCache: Boolean) {
        val bbox = tiles.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        val bboxString = "${bbox.min.latitude.format(7)}, ${bbox.min.longitude.format(7)} -> ${bbox.max.latitude.format(7)}, ${bbox.max.longitude.format(7)}"

        if (!ignoreCache && hasDownloadedAlready(tiles)) {
            Log.i(TAG, "Not downloading ($bboxString), data still fresh")
            return
        }
        Log.i(TAG, "Starting download ($bboxString)")

        val time = currentTimeMillis()

        coroutineScope {
            // all downloaders run concurrently
            launch { notesDownloader.download(bbox) }
            launch { mapDataDownloader.download(bbox) }
            launch { mapTilesDownloader.download(bbox) }
        }
        putDownloadedAlready(tiles)

        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Finished download ($bboxString) in ${seconds.format(1)}s")
    }

    private fun hasDownloadedAlready(tiles: TilesRect): Boolean {
        val freshTime = ApplicationConstants.REFRESH_DATA_AFTER
        val ignoreOlderThan = max(0, currentTimeMillis() - freshTime)
        return downloadedTilesDb.get(tiles, ignoreOlderThan).contains(DownloadedTilesType.ALL)
    }

    private fun putDownloadedAlready(tiles: TilesRect) {
        downloadedTilesDb.put(tiles, DownloadedTilesType.ALL)
    }

    companion object {
        private const val TAG = "Download"
    }
}
