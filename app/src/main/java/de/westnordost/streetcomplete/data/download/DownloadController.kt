package de.westnordost.streetcomplete.data.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox

/** Controls downloading */
class DownloadController(
    private val context: Context
) : DownloadProgressSource {

    private var downloadServiceIsBound: Boolean = false
    private var downloadService: DownloadService.Interface? = null
    private val downloadServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            downloadService = service as DownloadService.Interface
            downloadService?.setProgressListener(downloadProgressRelay)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            downloadService = null
        }
    }
    private val downloadProgressRelay = DownloadProgressRelay()

    /** @return true if a download triggered by the user is running */
    override val isPriorityDownloadInProgress: Boolean get() =
        downloadService?.isPriorityDownloadInProgress == true

    /** @return true if a download is running */
    override val isDownloadInProgress: Boolean get() =
        downloadService?.isDownloadInProgress == true

    var showNotification: Boolean
        get() = downloadService?.showDownloadNotification == true
        set(value) { downloadService?.showDownloadNotification = value }

    init {
        bindServices()
    }

    /** Download in at least the given bounding box asynchronously. The next-bigger rectangle
     * in a (z16) tiles grid that encloses the given bounding box will be downloaded.
     *
     * @param bbox the minimum area to download
     * @param isPriority whether this shall be a priority download (cancels previous downloads and
     * puts itself in the front)
     */
    fun download(bbox: BoundingBox, isPriority: Boolean = false) {
        if (downloadService == null) return

        val tilesRect = bbox.enclosingTilesRect(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        context.startService(DownloadService.createIntent(context, tilesRect, isPriority))
    }

    private fun bindServices() {
        downloadServiceIsBound = context.bindService(
            Intent(context, DownloadService::class.java),
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
