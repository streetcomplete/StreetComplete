package de.westnordost.streetcomplete.data.maptiles

import androidx.compose.runtime.snapshotFlow
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.screens.main.map.toGeoJsonBoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first
import org.maplibre.compose.offline.DownloadProgress
import org.maplibre.compose.offline.DownloadStatus
import org.maplibre.compose.offline.OfflineManager
import org.maplibre.compose.offline.OfflinePackDefinition

class MapLibreMapTilesDownloader(
    private val manager: OfflineManager,
    private val pixelRatio: Float,
) : MapTilesDownloader {

    override suspend fun download(bbox: BoundingBox) {
        val pack = manager.create(
            definition = OfflinePackDefinition.TilePyramid(
                styleUrl = STYLE_URL,
                bounds = bbox.toGeoJsonBoundingBox(),
                minZoom = 0,
                maxZoom = 16,
                //TODO maplibre-compose: pixelRatio = pixelRatio,
            ),
            // store timestamp as metadata for deleting areas older than X
            metadata = nowAsEpochMilliseconds().toString().encodeToByteArray(),
        )
        val startedAt = nowAsEpochMilliseconds()
        try {
            manager.resume(pack)

            val finalState = snapshotFlow { pack.downloadProgress }.first { it.isFinished }
            when (finalState) {
                is DownloadProgress.Healthy -> {
                    val seconds = (nowAsEpochMilliseconds() - startedAt) / 1000.0
                    Log.i(
                        TAG,
                        "Downloaded ${finalState.completedTileCount} tiles " +
                        "(${finalState.completedTileBytes / 1000}kB) in ${seconds.format(1)}s",
                    )
                }
                is DownloadProgress.Error -> {
                    error("MapLibre offline download failed (${finalState.reason}): ${finalState.message}")
                }
                is DownloadProgress.TileLimitExceeded -> {
                    error("MapLibre offline tile limit ${finalState.limit} was exceeded")
                }
                DownloadProgress.Unknown -> {
                    error("Unexpected terminal offline progress")
                }
            }
        } catch (error: CancellationException) {
            manager.pause(pack)
            throw error
        } catch (error: Exception) {
            manager.pause(pack)
            Log.w(TAG, error.message.orEmpty(), error)
            throw error
        }
    }

    override suspend fun deleteOld(time: Long) {
        val packs = manager.packs.toList()
        for (pack in packs) {
            val packTime = pack.metadata?.decodeToString()?.toLongOrNull()
            if (packTime == null || packTime < time) {
                manager.delete(pack)
            }
        }
    }

    override suspend fun clear() {
        try {
            val packs = manager.packs.toList()
            for (pack in packs) { manager.delete(pack) }
            manager.clearAmbientCache()
        } catch (error: Exception) {
            Log.w(TAG, error.message.orEmpty(), error)
        }
    }

    private companion object {
        private const val TAG = "MapTilesDownload"

        private const val STYLE_URL = "https://streetcomplete.app/map-jawg/streetcomplete.json"
    }
}

private val DownloadProgress.isFinished: Boolean get() = when (this) {
    is DownloadProgress.Healthy -> status == DownloadStatus.Complete
    is DownloadProgress.Error,
    is DownloadProgress.TileLimitExceeded -> true
    DownloadProgress.Unknown -> false
}
