package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import de.westnordost.streetcomplete.screens.main.map.components.DownloadedAreaMapComponent
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

class DownloadedAreaManager(
    private val ctrl: KtMapController,
    private val mapComponent: DownloadedAreaMapComponent,
    private val downloadedTilesSource: DownloadedTilesSource
) : DefaultLifecycleObserver {

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())
    private var hasUpdated: Boolean = false

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
            if (ctrl.cameraPosition.zoom <= 8) {
                hasUpdated = false
            } else {
                mapComponent.set(downloadedTilesSource.getAll(ApplicationConstants.DELETE_OLD_DATA_AFTER))
                hasUpdated = true
            }
        }
    }

    fun onNewScreenPosition() {
        // workaround for tangram bug that if the polygon is set while the zoom is ~ below zoom 6
        // no holes (= the downloaded areas) will be shown
        if (!hasUpdated) update()
    }
}
