package de.westnordost.streetcomplete.data.upload

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager

/** Controls uploading */
class UploadController(private val context: Context) {
    /** Collect and upload all changes made by the user  */
    fun upload(isUserInitiated: Boolean) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            Uploader.TAG,
            ExistingWorkPolicy.KEEP,
            UploadWorker.createWorkRequest(isUserInitiated)
        )
    }
}
