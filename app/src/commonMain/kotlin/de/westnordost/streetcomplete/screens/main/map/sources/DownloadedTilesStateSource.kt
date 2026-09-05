package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Common observable state for the tiles retained by StreetComplete's data-cleanup policy. */
class DownloadedTilesStateSource(
    private val downloadedTilesSource: DownloadedTilesSource,
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _tiles = MutableStateFlow<List<TilePos>>(emptyList())
    val tiles: StateFlow<List<TilePos>> = _tiles.asStateFlow()

    private var reloadJob: Job? = null
    private val stateLock = ReentrantLock()
    private var reloadGeneration = 0L
    private var isActive = false
    private var isClosed = false

    private val listener = object : DownloadedTilesSource.Listener {
        override fun onUpdated() = reload()
    }

    /** Starts or stops observing retained tiles with the map's presentation lifecycle. */
    fun setActive(active: Boolean) {
        stateLock.withLock {
            if (isClosed || isActive == active) return
            isActive = active
            ++reloadGeneration
            reloadJob?.cancel()
            if (active) {
                downloadedTilesSource.addListener(listener)
                reload()
            } else {
                downloadedTilesSource.removeListener(listener)
            }
        }
    }

    fun close() {
        stateLock.withLock {
            if (isClosed) return
            isClosed = true
            ++reloadGeneration
            reloadJob?.cancel()
            if (isActive) downloadedTilesSource.removeListener(listener)
        }
        scope.cancel()
    }

    private fun reload() {
        stateLock.withLock {
            if (isClosed || !isActive) return
            reloadJob?.cancel()
            val generation = ++reloadGeneration
            reloadJob = scope.launch {
                val tiles = downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
                currentCoroutineContext().ensureActive()
                stateLock.withLock {
                    if (!isClosed && isActive && generation == reloadGeneration) {
                        _tiles.value = tiles
                    }
                }
            }
        }
    }
}
