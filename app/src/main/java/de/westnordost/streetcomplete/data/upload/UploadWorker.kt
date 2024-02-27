package de.westnordost.streetcomplete.data.upload

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.sync.createSyncNotification

/** Collects and uploads all user changes: notes left, comments left on existing
 * notes and quests answered  */
class UploadWorker(
    private val uploader: Uploader,
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationId = ApplicationConstants.NOTIFICATIONS_ID_SYNC
        val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
        val notification = createSyncNotification(context, cancelIntent)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            uploader.upload()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun createWorkRequest(isUserInitiated: Boolean): OneTimeWorkRequest {
            val builder = OneTimeWorkRequestBuilder<UploadWorker>()
            if (isUserInitiated) builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            return builder.build()
        }
    }
}
