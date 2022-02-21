package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ChangesetAutoCloser(private val context: Context) {

    fun enqueue(delayInMilliseconds: Long) {
        // changesets are closed delayed after X minutes of inactivity
        WorkManager.getInstance(context).enqueueUniqueWork("AutoCloseChangesets", REPLACE,
            OneTimeWorkRequest.Builder(ChangesetAutoCloserWorker::class.java)
                .setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
        )
    }
}
