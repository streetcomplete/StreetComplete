package de.westnordost.streetcomplete.data.download

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.DownloadWorker.Companion.ARG_IS_PRIORITY
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.util.Listeners

/** Controls downloading */
class DownloadController(private val context: Context) : DownloadProgressSource {

    private sealed interface State
    private data object None : State
    private data class InProgress(val hasPriority: Boolean) : State

    private var state: State = None

    private val listeners = Listeners<DownloadProgressSource.Listener>()
    private val workInfos: LiveData<List<WorkInfo>> get() =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(DownloadWorker.TAG)

    /** @return true if a download triggered by the user is running */
    override val isPriorityDownloadInProgress: Boolean get() =
        (state as? InProgress)?.hasPriority == true

    /** @return true if a download is running */
    override val isDownloadInProgress: Boolean get() =
        state is InProgress

    init {
        workInfos.observeForever { workInfos ->
            val nowInProgress = workInfos.any { !it.state.isFinished }
            val hasPriority = nowInProgress && workInfos.any {
                it.progress.getBoolean(ARG_IS_PRIORITY, false)
            }
            if (state !is InProgress && nowInProgress) {
                state = InProgress(hasPriority)
                listeners.forEach { it.onStarted() }
            } else if (state is InProgress && !nowInProgress) {
                state = None
                listeners.forEach { it.onFinished() }
            }
        }
    }

    /** Download in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z16) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param isPriority whether this shall be a priority download (cancels previous downloads and
     * puts itself in the front)
     */
    fun download(bbox: BoundingBox, isPriority: Boolean = false) {
        val tilesRect = bbox.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadWorker.TAG,
            if (isPriority) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            DownloadWorker.createWorkRequest(tilesRect, isPriority)
        )
    }

    override fun addListener(listener: DownloadProgressSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: DownloadProgressSource.Listener) {
        listeners.remove(listener)
    }
}
