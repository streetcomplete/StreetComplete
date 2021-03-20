package de.westnordost.streetcomplete.data.upload

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import de.westnordost.osmapi.map.data.LatLon

import javax.inject.Inject

import de.westnordost.streetcomplete.Injector
import kotlinx.coroutines.*

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 * notes and quests he answered  */
class UploadService : IntentService(TAG) {
    @Inject internal lateinit var uploader: Uploader

    private val binder = Interface()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isUploading: Boolean = false
    private var progressListener: UploadProgressListener? = null
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

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onHandleIntent(intent: Intent?) {
        isUploading = true
        progressListener?.onStarted()

        try {
            runBlocking {
                // launching in own scope so that if the scope is cancelled, the work can be cancelled too
                scope.launch {
                    uploader.upload()
                }.join()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload", e)
            progressListener?.onError(e)
        }

        isUploading = false
        progressListener?.onFinished()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    /** Public interface to classes that are bound to this service  */
    inner class Interface : Binder() {
        fun setProgressListener(listener: UploadProgressListener?) {
            progressListener = listener
        }

        val isUploadInProgress: Boolean get() = isUploading
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, UploadService::class.java)
        }

        private const val TAG = "Upload"
    }
}
