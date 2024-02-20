package de.westnordost.streetcomplete.data.download

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.sync.createSyncNotification
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Downloads all quests and tiles in a given area asynchronously.
 *
 * Generally, starting a new download cancels the old one. This is a feature; Consideration:
 * If the user requests a new area to be downloaded, he'll generally be more interested in his last
 * request than any request he made earlier and he wants that as fast as possible.
 */
class DownloadWorker(
    private val downloader: Downloader,
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private var isPriorityDownload: Boolean = false

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationId = ApplicationConstants.NOTIFICATIONS_ID_SYNC
        val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(id)
        val notification = createSyncNotification(context, cancelIntent)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    override suspend fun doWork(): Result {
        val tiles: TilesRect =
            inputData.getString(ARG_TILES_RECT)?.let { Json.decodeFromString(it) }
            ?: return Result.failure()

        try {
            isPriorityDownload = inputData.getBoolean(ARG_IS_PRIORITY, false)
            downloader.download(tiles, isPriorityDownload)
        } catch (e: CancellationException) {
            Log.i(TAG, "Download cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Unable to download", e)
            return Result.failure()
        } finally {
            // downloading flags must be set to false before invoking the callbacks
            isPriorityDownload = false
        }

        return Result.success()
    }

    companion object {
        const val TAG = "Download"
        const val ARG_TILES_RECT = "tilesRect"
        const val ARG_IS_PRIORITY = "isPriority"

        fun createWorkRequest(tilesRect: TilesRect, isPriority: Boolean): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<DownloadWorker>()
                .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
                .setInputData(workDataOf(
                    ARG_TILES_RECT to Json.encodeToString(tilesRect),
                    ARG_IS_PRIORITY to isPriority,
                ))
                .build()
    }
}
