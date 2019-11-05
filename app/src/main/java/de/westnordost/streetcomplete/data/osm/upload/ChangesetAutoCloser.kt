package de.westnordost.streetcomplete.data.osm.upload

import java.util.concurrent.TimeUnit

import javax.inject.Inject

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

import androidx.work.ExistingWorkPolicy.REPLACE

class ChangesetAutoCloser @Inject constructor() {

    fun enqueue(delayInMilliseconds: Long) {
        // changesets are closed delayed after X minutes of inactivity
        WorkManager.getInstance().enqueueUniqueWork("AutoCloseChangesets", REPLACE,
            OneTimeWorkRequest.Builder(ChangesetAutoCloserWorker::class.java)
                .setInitialDelay(delayInMilliseconds, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
        )
    }
}
