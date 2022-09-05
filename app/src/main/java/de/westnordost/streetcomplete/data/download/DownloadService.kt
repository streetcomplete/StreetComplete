package de.westnordost.streetcomplete.data.download

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants.NOTIFICATIONS_ID_SYNC
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.sync.CoroutineIntentService
import de.westnordost.streetcomplete.data.sync.createSyncNotification
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject

/** Downloads all quests and tiles in a given area asynchronously. To use, start the service with
 * the appropriate parameters.
 *
 * Generally, starting a new download cancels the old one. This is a feature; Consideration:
 * If the user requests a new area to be downloaded, he'll generally be more interested in his last
 * request than any request he made earlier and he wants that as fast as possible.
 *
 * The service can be bound to snoop into the state of the downloading process:
 * * To receive progress callbacks
 * * To receive callbacks when new quests are created or old ones removed
 * * To query for the state of the service and/or current download task, i.e. if the current
 * download job was started by the user
 */
class DownloadService : CoroutineIntentService(TAG) {
    private val downloader: Downloader by inject()

    // interface
    private val binder = Interface()

    // listener
    private var progressListener: DownloadProgressListener? = null

    // state
    private var isPriorityDownload: Boolean = false
    private var isDownloading: Boolean = false
        set(value) {
            field = value
            updateShowNotification()
        }

    private var showNotification = false
        set(value) {
            field = value
            updateShowNotification()
        }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override suspend fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        val tiles: TilesRect = Json.decodeFromString(intent.getStringExtra(ARG_TILES_RECT)!!)

        var error: Exception? = null
        try {
            isPriorityDownload = intent.getBooleanExtra(ARG_IS_PRIORITY, false)
            isDownloading = true

            progressListener?.onStarted()

            downloader.download(tiles, isPriorityDownload)
        } catch (e: CancellationException) {
            Log.i(TAG, "Download cancelled")
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                // ok. Nevermind then.
            } else {
                Log.e(TAG, "Unable to download", e)
                error = e
            }
        } finally {
            // downloading flags must be set to false before invoking the callbacks
            isPriorityDownload = false
            isDownloading = false
        }

        if (error != null) {
            progressListener?.onError(error)
        } else {
            progressListener?.onSuccess()
        }

        progressListener?.onFinished()
    }

    private fun updateShowNotification() {
        if (!showNotification || !isDownloading) stopForeground(true)
        else startForeground(NOTIFICATIONS_ID_SYNC, createSyncNotification(this))
    }

    /** Public interface to classes that are bound to this service  */
    inner class Interface : Binder() {
        fun setProgressListener(listener: DownloadProgressListener?) {
            progressListener = listener
        }

        val isPriorityDownloadInProgress: Boolean get() = isPriorityDownload

        val isDownloadInProgress: Boolean get() = isDownloading

        var showDownloadNotification: Boolean
            get() = showNotification
            set(value) { showNotification = value }
    }

    companion object {
        private const val TAG = "Download"
        const val ARG_TILES_RECT = "tilesRect"
        const val ARG_IS_PRIORITY = "isPriority"

        fun createIntent(context: Context, tilesRect: TilesRect, isPriority: Boolean): Intent {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(ARG_TILES_RECT, Json.encodeToString(tilesRect))
            intent.putExtra(ARG_IS_PRIORITY, isPriority)
            // priority download should cancel any earlier download
            intent.putExtra(ARG_PREVIOUS_CANCEL, isPriority)
            return intent
        }
    }
}
