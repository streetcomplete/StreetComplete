package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class MainMapViewModel : ViewModel() {
    abstract val downloadedTiles: StateFlow<Collection<TilePos>>
}

class MainMapViewModelImpl(
    private val downloadedTilesSource: DownloadedTilesSource
) : MainMapViewModel() {
    override val downloadedTiles: StateFlow<Collection<TilePos>> = callbackFlow {
        val listener = object : DownloadedTilesSource.Listener {
            override fun onUpdated() {
                launch {
                    val tiles = withContext(Dispatchers.IO) {
                        downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
                    }
                    trySend(tiles)
                }
            }
        }
        downloadedTilesSource.addListener(listener)
        awaitClose { downloadedTilesSource.removeListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
