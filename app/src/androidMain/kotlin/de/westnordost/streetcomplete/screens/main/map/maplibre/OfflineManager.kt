package de.westnordost.streetcomplete.screens.main.map.maplibre

import org.maplibre.android.offline.OfflineManager
import org.maplibre.android.offline.OfflineRegion
import org.maplibre.android.offline.OfflineRegionDefinition
import org.maplibre.android.offline.OfflineRegionError
import org.maplibre.android.offline.OfflineRegionStatus
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OfflineManagerException(message: String) : Exception(message)

suspend fun OfflineManager.awaitGetOfflineRegions(): List<OfflineRegion> = suspendCoroutine { cont ->
    listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
        override fun onError(error: String) {
            cont.resumeWithException(OfflineManagerException(error))
        }

        override fun onList(offlineRegions: Array<OfflineRegion>?) {
            cont.resume(offlineRegions?.toList().orEmpty())
        }
    })
}

suspend fun OfflineManager.awaitCreateOfflineRegion(
    definition: OfflineRegionDefinition,
    metadata: ByteArray
): OfflineRegion = suspendCoroutine { cont ->
    createOfflineRegion(definition, metadata, object : OfflineManager.CreateOfflineRegionCallback {
        override fun onCreate(offlineRegion: OfflineRegion) {
            cont.resume(offlineRegion)
        }

        override fun onError(error: String) {
            cont.resumeWithException(OfflineManagerException(error))
        }
    })
}

suspend fun OfflineManager.awaitResetDatabase(): Unit = suspendCoroutine { cont ->
    resetDatabase(object : OfflineManager.FileSourceCallback {
        override fun onError(message: String) {
            cont.resumeWithException(OfflineManagerException(message))
        }

        override fun onSuccess() {
            cont.resume(Unit)
        }
    })
}

suspend fun OfflineRegion.awaitDelete(): Unit = suspendCoroutine { cont ->
    delete(object : OfflineRegion.OfflineRegionDeleteCallback {
        override fun onDelete() {
            cont.resume(Unit)
        }

        override fun onError(error: String) {
            cont.resumeWithException(OfflineManagerException(error))
        }
    })
}

suspend fun OfflineRegion.awaitDownload(): OfflineRegionStatus = suspendCoroutine { cont ->
    var observing = true

    fun shouldContinue(): Boolean = synchronized(this) {
        if (!observing) return false
        observing = false
        return true
    }

    setObserver(object : OfflineRegion.OfflineRegionObserver {
        override fun mapboxTileCountLimitExceeded(limit: Long) {
            if (!shouldContinue()) return
            setObserver(null)
            setDownloadState(OfflineRegion.STATE_INACTIVE)
            cont.resumeWithException(OfflineManagerException("Tile count limit of $limit tiles exceeded"))
        }

        override fun onError(error: OfflineRegionError) {
            if (!shouldContinue()) return
            setObserver(null)
            setDownloadState(OfflineRegion.STATE_INACTIVE)
            cont.resumeWithException(OfflineManagerException(error.message))
        }

        override fun onStatusChanged(status: OfflineRegionStatus) {
            if (status.isComplete) {
                if (!shouldContinue()) return
                setObserver(null)
                setDownloadState(OfflineRegion.STATE_INACTIVE)
                cont.resume(status)
            }
        }
    })
    setDownloadState(OfflineRegion.STATE_ACTIVE)
}

/**
 *  Delete regions, which allows contained tiles to be deleted if cache size is exceeded.
 *  Assumes that download date is stored as string in [OfflineRegion.metadata].
 */
suspend fun OfflineManager.deleteRegionsOlderThan(olderThan: Long) {
    for (offlineRegion in awaitGetOfflineRegions()) {
        val timestamp = offlineRegion.metadata.toString(Charsets.UTF_8).toLongOrNull() ?: 0
        if (timestamp < olderThan) {
            offlineRegion.awaitDelete()
        }
    }
}
