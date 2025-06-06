package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import java.util.concurrent.TimeUnit

class ChangesetAutoCloserAndroid(private val context: Context): ChangesetAutoCloser {
    override fun enqueue(delayInMilliseconds: Long) {
        WorkManager.getInstance(context).enqueueUniqueWork("AutoCloseChangesets", REPLACE,
            OneTimeWorkRequestBuilder<ChangesetAutoCloserWorker>()
                .setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
        )
    }
}

class ChangesetAutoCloserWorker(
    private val openChangesetsManager: OpenChangesetsManager,
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            openChangesetsManager.closeOldChangesets()
        } catch (e: ConnectionException) {
            // wasn't able to connect to the server (i.e. connection timeout). Oh well, then,
            // never mind. Could also retry later with Result.retry() but the OSM API closes open
            // changesets after 1 hour anyway.
        } catch (e: AuthorizationException) {
            // the user may not be authorized yet (or not be authorized anymore) #283
            // nothing we can do about here. He will have to re-authenticate when he next opens the
            // app
            return Result.failure()
        }
        return Result.success()
    }
}
