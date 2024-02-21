package de.westnordost.streetcomplete.data.upload

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import de.westnordost.streetcomplete.util.logs.Log

/** Collects and uploads all user changes: notes left, comments left on existing
 * notes and quests answered  */
class UploadWorker(
    private val uploader: Uploader,
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            uploader.upload()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun createWorkRequest(): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<UploadWorker>().build()
    }
}
