package de.westnordost.streetcomplete.data.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.util.enclosingTilesRect
import javax.inject.Inject
import javax.inject.Singleton

/** Controls quest downloading */
@Singleton class QuestDownloadController @Inject constructor(
    private val context: Context
): DownloadProgressSource {

    private var downloadServiceIsBound: Boolean = false
    private var downloadService: QuestDownloadService.Interface? = null
    private val downloadServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            downloadService = service as QuestDownloadService.Interface
            downloadService?.setProgressListener(downloadProgressRelay)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            downloadService = null
        }
    }
    private val downloadProgressRelay = DownloadProgressRelay()

    /** @return true if a quest download triggered by the user is running */
    override val isPriorityDownloadInProgress: Boolean get() =
        downloadService?.isPriorityDownloadInProgress == true

    /** @return true if a quest download is running */
    override val isDownloadInProgress: Boolean get() =
        downloadService?.isDownloadInProgress == true

    /** @return the item that is currently being downloaded or null if nothing is downloaded */
    override val currentDownloadItem: DownloadItem? get() =
        downloadService?.currentDownloadItem

    var showNotification: Boolean
        get() = downloadService?.showDownloadNotification == true
        set(value) { downloadService?.showDownloadNotification = value }

    init {
        bindServices()
    }

    /** Download quests in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z16) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param isPriority whether this shall be a priority download (cancels previous downloads and
     * puts itself in the front)
     */
    fun download(bbox: BoundingBox, isPriority: Boolean = false) {
        val tilesRect = bbox.enclosingTilesRect(ApplicationConstants.QUEST_TILE_ZOOM)
        context.startService(QuestDownloadService.createIntent(context, tilesRect, isPriority))
    }

    private fun bindServices() {
        downloadServiceIsBound = context.bindService(
            Intent(context, QuestDownloadService::class.java),
            downloadServiceConnection, Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindServices() {
        if (downloadServiceIsBound) context.unbindService(downloadServiceConnection)
        downloadServiceIsBound = false
    }

    override fun addDownloadProgressListener(listener: DownloadProgressListener) {
        downloadProgressRelay.addListener(listener)
    }
    override fun removeDownloadProgressListener(listener: DownloadProgressListener) {
        downloadProgressRelay.removeListener(listener)
    }
}