package de.westnordost.streetcomplete.data.download

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Binder
import android.os.IBinder
import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.VisibleQuestRelay
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider

/** Downloads all quests in a given area asynchronously. To use, start the service with the
 * appropriate parameters.
 *
 * Generally, starting a new download cancels the old one. This is a feature; Consideration:
 * If the user requests a new area to be downloaded, he'll generally be more interested in his last
 * request than any request he made earlier and he wants that as fast as possible. (Downloading
 * in-parallel is not possible with Overpass, only one request a time is allowed on the public
 * instance)
 *
 * The service can be bound to snoop into the state of the downloading process:
 * * To receive progress callbacks
 * * To receive callbacks when new quests are created or old ones removed
 * * To query for the state of the service and/or current download task, i.e. if the current
 * download job was started by the user
 */
class QuestDownloadService : SingleIntentService(TAG) {
    @Inject internal lateinit var questDownloaderProvider: Provider<QuestDownloader>

    // interface
    private val binder: IBinder = Interface()

    // listeners
    private lateinit var progressListenerRelay: QuestDownloadProgressRelay
    private val visibleQuestRelay = VisibleQuestRelay()

    // state
    private var isPriorityDownload: Boolean = false
    private var isDownloading: Boolean = false

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        progressListenerRelay = QuestDownloadProgressRelay(
            QuestDownloadNotification(this, ApplicationConstants.NOTIFICATIONS_CHANNEL_DOWNLOAD, 1)
        )
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onHandleIntent(intent: Intent?, cancelState: AtomicBoolean) {
        if (cancelState.get()) return
        if (intent == null) return
        if (intent.getBooleanExtra(ARG_CANCEL, false)) {
            cancel()
            Log.i(TAG, "Download cancelled")
            return
        }

        val tiles = intent.getParcelableExtra<Rect>(ARG_TILES_RECT)
        val maxQuestTypes =
            if (intent.hasExtra(ARG_MAX_QUEST_TYPES)) intent.getIntExtra(ARG_MAX_QUEST_TYPES, 0)
            else null

        val dl = questDownloaderProvider.get()
        dl.progressListener = progressListenerRelay
        dl.questListener = visibleQuestRelay
        try {
            isPriorityDownload = intent.hasExtra(ARG_IS_PRIORITY)
            isDownloading = true
            dl.download(tiles, maxQuestTypes, cancelState)
            isPriorityDownload = false
            isDownloading = false
        } catch (e: Exception) {
            Log.e(TAG, "Unable to download quests", e)
            progressListenerRelay.onError(e)
        }
    }

    /** Public interface to classes that are bound to this service  */
    inner class Interface : Binder() {
        fun setProgressListener(listener: QuestDownloadProgressListener?) {
            progressListenerRelay.listener = listener
        }

        fun setQuestListener(listener: VisibleQuestListener?) {
            visibleQuestRelay.listener = listener
        }

        val isPriorityDownloadInProgress: Boolean get() = isPriorityDownload

        val isDownloadInProgress: Boolean get() = isDownloading

        fun startForeground() {
            progressListenerRelay.startForeground()
        }

        fun stopForeground() {
            progressListenerRelay.stopForeground()
        }
    }

    companion object {
        private const val TAG = "QuestDownload"
        const val ARG_TILES_RECT = "tilesRect"
        const val ARG_MAX_QUEST_TYPES = "maxQuestTypes"
        const val ARG_IS_PRIORITY = "isPriority"
        const val ARG_CANCEL = "cancel"

        fun createIntent(context: Context, tilesRect: Rect?, maxQuestTypesToDownload: Int?, isPriority: Boolean): Intent {
            val intent = Intent(context, QuestDownloadService::class.java)
            intent.putExtra(ARG_TILES_RECT, tilesRect)
            intent.putExtra(ARG_IS_PRIORITY, isPriority)
            maxQuestTypesToDownload?.let { intent.putExtra(ARG_MAX_QUEST_TYPES, it) }
            return intent
        }

        fun createCancelIntent(context: Context): Intent {
            val intent = Intent(context, QuestDownloadService::class.java)
            intent.putExtra(ARG_CANCEL, true)
            return intent
        }
    }
}
