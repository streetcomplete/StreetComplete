package de.westnordost.streetcomplete.data.upload

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants.NOTIFICATIONS_ID_SYNC

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.sync.CoroutineIntentService
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.sync.createSyncNotification

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 * notes and quests he answered  */
class UploadService : CoroutineIntentService(TAG) {
    @Inject internal lateinit var uploader: Uploader

    private lateinit var notification: Notification

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
        Injector.applicationComponent.inject(this)
        uploader.uploadedChangeListener = uploadedChangeRelay
    }

    override fun onCreate() {
        super.onCreate()
        notification = createSyncNotification(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override suspend fun onHandleIntent(intent: Intent?) {
        isUploading = true
        progressListener?.onStarted()

        try {
            uploader.upload()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload", e)
            progressListener?.onError(e)
        }

        isUploading = false
        progressListener?.onFinished()
    }

    private fun updateShowNotification() {
        if (!showNotification || !isUploading) stopForeground(true)
        else startForeground(NOTIFICATIONS_ID_SYNC, notification)
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
