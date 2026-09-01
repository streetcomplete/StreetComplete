package de.westnordost.streetcomplete.data.maptiles

import androidx.compose.runtime.snapshotFlow
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.offline.DownloadProgress
import org.maplibre.compose.offline.DownloadStatus
import org.maplibre.compose.offline.OfflineManager
import org.maplibre.compose.offline.OfflinePackDefinition
import org.maplibre.spatialk.geojson.BoundingBox as MapLibreBoundingBox

/** Stores StreetComplete's base style through MapLibre Compose's shared offline-pack API. */
class MapLibreMapTilesDownloader(
    runtime: MapRuntime,
    private val pixelRatio: Float,
) : MapTilesDownloader {
    private val manager: OfflineManager = runtime.offlineManager

    override suspend fun deleteOld(time: Long) {
        for (pack in manager.packs.toList()) {
            val packTime = pack.metadata?.decodeToString()?.toLongOrNull()
            if (packTime == null || packTime < time) manager.delete(pack)
        }
    }

    override suspend fun clear() {
        try {
            manager.packs.toList().forEach { manager.delete(it) }
            manager.clearAmbientCache()
        } catch (error: Exception) {
            Log.w(TAG, error.message.orEmpty(), error)
        }
    }

    override suspend fun download(bbox: BoundingBox) {
        val pack = manager.create(
            definition = bbox.toOfflinePackDefinition(pixelRatio),
            metadata = nowAsEpochMilliseconds().toString().encodeToByteArray(),
        )
        val startedAt = nowAsEpochMilliseconds()
        try {
            manager.resume(pack)
            when (val progress = snapshotFlow { pack.downloadProgress }.first { it.isTerminal }) {
                is DownloadProgress.Healthy -> {
                    val seconds = (nowAsEpochMilliseconds() - startedAt) / 1000.0
                    Log.i(
                        TAG,
                        "Downloaded ${progress.completedTileCount} tiles " +
                            "(${progress.completedTileBytes / 1000}kB) in ${seconds.format(1)}s",
                    )
                }
                is DownloadProgress.Error -> {
                    error("MapLibre offline download failed (${progress.reason}): ${progress.message}")
                }
                is DownloadProgress.TileLimitExceeded -> {
                    error("MapLibre offline tile limit ${progress.limit} was exceeded")
                }
                DownloadProgress.Unknown -> error("Unexpected terminal offline progress")
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

    private companion object {
        const val TAG = "MapTilesDownload"
    }
}

internal fun BoundingBox.toOfflinePackDefinition(pixelRatio: Float) =
    OfflinePackDefinition.TilePyramid(
        styleUrl = "https://streetcomplete.app/map-jawg/streetcomplete.json",
        bounds = MapLibreBoundingBox(
            west = min.longitude,
            south = min.latitude,
            east = max.longitude,
            north = max.latitude,
        ),
        minZoom = 0,
        maxZoom = 16,
        pixelRatio = pixelRatio,
    )

private val DownloadProgress.isTerminal: Boolean
    get() = when (this) {
        is DownloadProgress.Healthy -> status == DownloadStatus.Complete
        is DownloadProgress.Error,
        is DownloadProgress.TileLimitExceeded -> true
        DownloadProgress.Unknown -> false
    }
