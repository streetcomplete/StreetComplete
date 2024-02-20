package de.westnordost.streetcomplete.data.download

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.upload.UploadWorker
import de.westnordost.streetcomplete.util.Listeners

/** Controls downloading */
class DownloadController(private val context: Context) : DownloadProgressSource {

    // TODO listener

    private val listeners = Listeners<DownloadProgressSource.Listener>()
    private val workInfos: LiveData<List<WorkInfo>> get() =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(DownloadWorker.TAG)

    /** @return true if a download triggered by the user is running */
    override val isPriorityDownloadInProgress: Boolean get() =
        TODO()

    /** @return true if a download is running */
    override val isDownloadInProgress: Boolean get() =
        workInfos.value?.any { !it.state.isFinished } == true

    init {
        workInfos.observeForever { workInfos ->
            TODO()
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
            if (isPriority) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.APPEND,
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
