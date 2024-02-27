package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ChangesetAutoCloser(private val context: Context) {

    fun enqueue(delayInMilliseconds: Long) {
        // changesets are closed delayed after X minutes of inactivity
        WorkManager.getInstance(context).enqueueUniqueWork("AutoCloseChangesets", REPLACE,
            OneTimeWorkRequestBuilder<ChangesetAutoCloserWorker>()
                .setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
                .build()
        )
    }
}
