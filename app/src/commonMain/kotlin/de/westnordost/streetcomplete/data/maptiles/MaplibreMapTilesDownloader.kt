package de.westnordost.streetcomplete.data.maptiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.map.MapTiles
import de.westnordost.streetcomplete.screens.main.map.toGeoJsonBoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.flow.first
import org.maplibre.compose.offline.DownloadProgress
import org.maplibre.compose.offline.DownloadStatus
import org.maplibre.compose.offline.OfflineManager
import org.maplibre.compose.offline.OfflinePack
import org.maplibre.compose.offline.OfflinePackDefinition
import kotlin.coroutines.cancellation.CancellationException

class MapLibreMapTilesDownloader(
    private val manager: OfflineManager,
    private val pixelRatio: Float,
) : MapTilesDownloader {

    override suspend fun download(bbox: BoundingBox) {
        var pack: OfflinePack? = null
        try {
            pack = manager.create(
                definition = OfflinePackDefinition.TilePyramid(
                    // Only tiles need downloading; glyphs and images are packaged with the app.
                    styleUrl = Res.getUri("files/map-download-style.json"),
                    bounds = bbox.toGeoJsonBoundingBox(),
                    minZoom = 0,
                    maxZoom = MapTiles.MAX_ZOOM,
                    pixelRatio = pixelRatio,
                ),
                // store timestamp as metadata for deleting areas older than X
                metadata = nowAsEpochMilliseconds().toString().encodeToByteArray(),
            )
            val startedAt = nowAsEpochMilliseconds()
            manager.resume(pack)

            val finalState = pack.downloadProgress.first { it.isFinished }
            when (finalState) {
                is DownloadProgress.Healthy -> {
                    val seconds = (nowAsEpochMilliseconds() - startedAt) / 1000.0
                    Log.i(
                        TAG,
                        "Downloaded ${finalState.completedTileCount} tiles " +
                        "(${finalState.completedTileBytes / 1000}kB) in ${seconds.format(1)}s",
                    )
                }
                is DownloadProgress.Error ->
                    Log.w(TAG, "Offline download failed (${finalState.reason}): ${finalState.message}")
                is DownloadProgress.TileLimitExceeded ->
                    Log.w(TAG, "Offline tile limit ${finalState.limit} was exceeded")
                DownloadProgress.Unknown -> Unit // not a finished state
            }
        } catch (error: Exception) {
            try {
                pack?.let { manager.pause(it) }
            } catch (pauseError: Exception) {
                error.addSuppressed(pauseError)
            }
            // Map tiles are optional: a failed pack must neither fail the download of the other
            // data nor keep the area from counting as downloaded
            if (error is CancellationException) throw error
            Log.w(TAG, error.message.orEmpty(), error)
        }
    }

    override suspend fun deleteOld(time: Long) {
        try {
            for (pack in manager.packs.value) {
                val packTime = pack.metadata.value?.decodeToString()?.toLongOrNull()
                if (packTime == null || packTime < time) {
                    manager.delete(pack)
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, error.message.orEmpty(), error)
        }
    }

    override suspend fun clear() {
        try {
            for (pack in manager.packs.value) { manager.delete(pack) }
            manager.clearAmbientCache()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w(TAG, error.message.orEmpty(), error)
        }
    }

    private companion object {
        private const val TAG = "MapTilesDownload"
    }
}

private val DownloadProgress.isFinished: Boolean get() = when (this) {
    is DownloadProgress.Healthy -> status == DownloadStatus.Complete
    is DownloadProgress.Error,
    is DownloadProgress.TileLimitExceeded -> true
    DownloadProgress.Unknown -> false
}
