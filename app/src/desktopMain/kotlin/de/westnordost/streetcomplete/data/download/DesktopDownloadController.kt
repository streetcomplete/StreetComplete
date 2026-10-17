package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

class DesktopDownloadController(private val downloader: Downloader) : DownloadController {
    // Downloader already logs errors and notifies its listeners
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, _ -> }
    )
    private var job: Job? = null

    override fun download(bbox: BoundingBox, isUserInitiated: Boolean) {
        scope.launch {
            // like on Android: a user-initiated download replaces the current one, others don't
            val previous = job
            if (!isUserInitiated && previous?.isActive == true) return@launch
            job = currentCoroutineContext().job
            previous?.cancelAndJoin()
            downloader.download(bbox, isUserInitiated)
        }
    }

    fun close() { scope.cancel() }
}
