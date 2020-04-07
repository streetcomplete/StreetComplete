package de.westnordost.streetcomplete.data.quest

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.*
import de.westnordost.streetcomplete.data.upload.UploadProgressListener
import de.westnordost.streetcomplete.data.upload.UploadProgressRelay
import de.westnordost.streetcomplete.data.upload.UploadProgressSource
import de.westnordost.streetcomplete.data.upload.UploadService
import de.westnordost.streetcomplete.util.enclosingTilesRect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class QuestUploadDownloadController @Inject constructor(
    private val context: Context
): UploadProgressSource, QuestDownloadProgressSource {

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
    private val downloadProgressRelay = QuestDownloadProgressRelay()

    private var uploadServiceIsBound = false
    private var uploadService: UploadService.Interface? = null
    private val uploadServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            uploadService = service as UploadService.Interface
            uploadService?.setProgressListener(uploadProgressRelay)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            uploadService = null
        }
    }
    private val uploadProgressRelay = UploadProgressRelay()

    /** @return true if a quest download triggered by the user is running */
    override val isPriorityDownloadInProgress: Boolean get() =
        downloadService?.isPriorityDownloadInProgress == true

    /** @return true if a quest download is running */
    override val isDownloadInProgress: Boolean get() =
        downloadService?.isDownloadInProgress == true

    override val downloadProgress: Float get() = downloadService?.downloadProgress ?: 0f

    var showNotification: Boolean
        get() = downloadService?.showDownloadNotification == true
        set(value) { downloadService?.showDownloadNotification = value }

    init {
        bindServices()
    }

    /** Download quests in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z14) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param maxQuestTypesToDownload download at most the given number of quest types. null for
     * unlimited
     * @param isPriority whether this shall be a priority download (cancels previous downloads and
     * puts itself in the front)
     */
    fun download(bbox: BoundingBox, maxQuestTypesToDownload: Int? = null, isPriority: Boolean = false) {
        val tilesRect = bbox.enclosingTilesRect(ApplicationConstants.QUEST_TILE_ZOOM)
        context.startService(QuestDownloadService.createIntent(context, tilesRect, maxQuestTypesToDownload, isPriority))
    }

    /** Collect and upload all changes made by the user  */
    fun upload() {
        context.startService(UploadService.createIntent(context))
    }

    private fun bindServices() {
        downloadServiceIsBound = context.bindService(
            Intent(context, QuestDownloadService::class.java),
            downloadServiceConnection, Context.BIND_AUTO_CREATE
        )
        uploadServiceIsBound = context.bindService(
            Intent(context, UploadService::class.java),
            uploadServiceConnection, Context.BIND_AUTO_CREATE
        )
    }

    private fun unbindServices() {
        if (downloadServiceIsBound) context.unbindService(downloadServiceConnection)
        downloadServiceIsBound = false
        if (uploadServiceIsBound) context.unbindService(uploadServiceConnection)
        uploadServiceIsBound = false
    }

    override fun addUploadProgressListener(listener: UploadProgressListener) {
        uploadProgressRelay.addListener(listener)
    }
    override fun removeUploadProgressListener(listener: UploadProgressListener) {
        uploadProgressRelay.removeListener(listener)
    }
    override fun addQuestDownloadProgressListener(listener: QuestDownloadProgressListener) {
        downloadProgressRelay.addListener(listener)
    }
    override fun removeQuestDownloadProgressListener(listener: QuestDownloadProgressListener) {
        downloadProgressRelay.removeListener(listener)
    }
}