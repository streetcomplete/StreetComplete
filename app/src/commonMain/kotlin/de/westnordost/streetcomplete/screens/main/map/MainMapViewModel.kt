package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
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
    override val downloadedTiles = MutableStateFlow<Collection<TilePos>>(emptyList())

    private val downloadedTilesListener = object : DownloadedTilesSource.Listener {
        override fun onUpdated() { updateDownloadedTiles() }
    }

    init {
        updateDownloadedTiles()
        downloadedTilesSource.addListener(downloadedTilesListener)
    }

    override fun onCleared() {
        downloadedTilesSource.removeListener(downloadedTilesListener)
    }

    private fun updateDownloadedTiles() {
        launch(Dispatchers.IO) {
            downloadedTiles.value = downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER)
        }
    }
}
