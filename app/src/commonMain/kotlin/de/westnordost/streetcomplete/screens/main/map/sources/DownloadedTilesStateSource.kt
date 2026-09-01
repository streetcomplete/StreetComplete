package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
    private var isClosed = false

    private val listener = object : DownloadedTilesSource.Listener {
        override fun onUpdated() = reload()
    }

    init {
        downloadedTilesSource.addListener(listener)
        reload()
    }

    fun close() {
        if (isClosed) return
        isClosed = true
        downloadedTilesSource.removeListener(listener)
        scope.cancel()
    }

    private fun reload() {
        if (isClosed) return
        reloadJob?.cancel()
        reloadJob = scope.launch {
            _tiles.value = downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
        }
    }
}
