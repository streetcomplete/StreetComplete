package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.screens.main.map.components.DownloadedAreaMapComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadedAreaManager(
    private val mapComponent: DownloadedAreaMapComponent,
    private val downloadedTilesSource: DownloadedTilesSource
) : DefaultLifecycleObserver {

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val downloadedTilesListener = object : DownloadedTilesSource.Listener {
        override fun onUpdated() {
            update()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        update()
        downloadedTilesSource.addListener(downloadedTilesListener)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        viewLifecycleScope.coroutineContext.cancelChildren()
        downloadedTilesSource.removeListener(downloadedTilesListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun update() {
        viewLifecycleScope.launch {
            val tiles = withContext(Dispatchers.IO) { downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER) }
            mapComponent.set(tiles)
        }
    }
}
