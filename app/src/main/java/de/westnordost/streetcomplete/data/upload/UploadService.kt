package de.westnordost.streetcomplete.data.upload

import android.app.ForegroundServiceStartNotAllowedException
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import de.westnordost.streetcomplete.ApplicationConstants.NOTIFICATIONS_ID_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.sync.CoroutineIntentService
import de.westnordost.streetcomplete.data.sync.createSyncNotification
import de.westnordost.streetcomplete.util.logs.Log
import org.koin.android.ext.android.inject

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 * notes and quests he answered  */
class UploadService : CoroutineIntentService(TAG) {
    private val uploader: Uploader by inject()

    // interface
    private val binder = Interface()

    // listener
    private var progressListener: UploadProgressListener? = null

    // state
    private var isUploading: Boolean = false
        set(value) {
            field = value
            updateShowNotification()
        }

    private var showNotification = false
        set(value) {
            field = value
            updateShowNotification()
        }

    private val uploadedChangeRelay = object : OnUploadedChangeListener {
        override fun onUploaded(questType: String, at: LatLon) {
            progressListener?.onProgress(true)
        }

        override fun onDiscarded(questType: String, at: LatLon) {
            progressListener?.onProgress(false)
        }
    }

    init {
        uploader.uploadedChangeListener = uploadedChangeRelay
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override suspend fun onHandleIntent(intent: Intent?) {
        try {
            isUploading = true
            progressListener?.onStarted()

            uploader.upload()
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is ForegroundServiceStartNotAllowedException) {
                // ok. Nevermind then.
            } else {
                Log.e(TAG, "Unable to upload", e)
                progressListener?.onError(e)
            }
        }

        isUploading = false
        progressListener?.onFinished()
    }

    private fun updateShowNotification() {
        if (!showNotification || !isUploading) {
            stopForeground(true)
        } else {
            startForeground(NOTIFICATIONS_ID_SYNC, createSyncNotification(this))
        }
    }

    /** Public interface to classes that are bound to this service  */
    inner class Interface : Binder() {
        fun setProgressListener(listener: UploadProgressListener?) {
            progressListener = listener
        }

        val isUploadInProgress: Boolean get() = isUploading

        var showUploadNotification: Boolean
            get() = showNotification
            set(value) { showNotification = value }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, UploadService::class.java)
        }

        private const val TAG = "Upload"
    }
}
