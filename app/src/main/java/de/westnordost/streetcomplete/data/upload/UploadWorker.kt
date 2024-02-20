package de.westnordost.streetcomplete.data.upload

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.flow.Flow

/** Collects and uploads all changes the user has done: notes he left, comments he left on existing
 * notes and quests he answered  */
class UploadWorker(
    private val uploader: Uploader,
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val uploadedChangeRelay = object : OnUploadedChangeListener {
        override fun onUploaded(questType: String, at: LatLon) {
            TODO()
        }

        override fun onDiscarded(questType: String, at: LatLon) {
            TODO()
        }
    }

    init {
        uploader.uploadedChangeListener = uploadedChangeRelay
    }

    override suspend fun doWork(): Result {
        try {
            uploader.upload()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to upload", e)
            return Result.failure()
        }
        return Result.success()
    }

    companion object {
        const val TAG = "Upload"

        fun createWorkRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<UploadWorker>().build()
    }
}
