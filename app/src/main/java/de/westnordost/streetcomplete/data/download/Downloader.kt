package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.maptiles.MapTilesDownloader
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataDownloader
import de.westnordost.streetcomplete.data.osmnotes.NotesDownloader
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.area
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max

/** Downloads all the things */
class Downloader(
    private val notesDownloader: NotesDownloader,
    private val mapDataDownloader: MapDataDownloader,
    private val mapTilesDownloader: MapTilesDownloader,
    private val downloadedTilesController: DownloadedTilesController,
    private val mutex: Mutex
) : DownloadProgressSource {

    private val listeners = Listeners<DownloadProgressSource.Listener>()

    override var isUserInitiatedDownloadInProgress: Boolean = false
        private set

    override var isDownloadInProgress: Boolean = false
        private set

    suspend fun download(bbox: BoundingBox, isUserInitiated: Boolean) {
        var hasError = false
        try {
            isDownloadInProgress = true
            isUserInitiatedDownloadInProgress = isUserInitiated
            listeners.forEach { it.onStarted() }

            val tiles = bbox.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
            val tilesBbox = tiles.asBoundingBox(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
            val bboxString = listOf(
                tilesBbox.min.latitude.format(7),
                tilesBbox.min.longitude.format(7),
                tilesBbox.max.latitude.format(7),
                tilesBbox.max.longitude.format(7)
            ).joinToString(",")
            val sqkm = (tilesBbox.area() / 1000 / 1000).format(1)

            if (!isUserInitiated && hasDownloadedAlready(tiles)) {
                Log.i(TAG, "Not downloading ($sqkm km², bbox: $bboxString), data still fresh")
                return
            }
            Log.i(TAG, "Starting download ($sqkm km², bbox: $bboxString)")

            val time = nowAsEpochMilliseconds()

            mutex.withLock {
                coroutineScope {
                    // all downloaders run concurrently
                    launch { notesDownloader.download(tilesBbox) }
                    launch { mapDataDownloader.download(tilesBbox) }
                    launch { mapTilesDownloader.download(tilesBbox) }
                }
            }
            putDownloadedAlready(tiles)

            val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
            Log.i(TAG, "Finished download ($sqkm km², bbox: $bboxString) in ${seconds.format(1)}s")
        } catch (e: CancellationException) {
            hasError = true
            Log.i(TAG, "Download cancelled")
        } catch (e: Exception) {
            hasError = true
            Log.e(TAG, "Unable to download", e)
            listeners.forEach { it.onError(e) }
            throw e
        } finally {
            isDownloadInProgress = false
            isUserInitiatedDownloadInProgress = false
            listeners.forEach { it.onFinished() }
            if (!hasError) listeners.forEach { it.onSuccess() }
        }
    }

    override fun addListener(listener: DownloadProgressSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: DownloadProgressSource.Listener) {
        listeners.remove(listener)
    }

    private fun hasDownloadedAlready(tiles: TilesRect): Boolean {
        val freshTime = ApplicationConstants.REFRESH_DATA_AFTER
        val ignoreOlderThan = max(0, nowAsEpochMilliseconds() - freshTime)
        return downloadedTilesController.contains(tiles, ignoreOlderThan)
    }

    private fun putDownloadedAlready(tiles: TilesRect) {
        downloadedTilesController.put(tiles)
    }

    companion object {
        const val TAG = "Download"
    }
}
