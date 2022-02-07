package de.westnordost.streetcomplete.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CleanerWorker(
    private val cleaner: Cleaner,
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        cleaner.clean()
        return Result.success()
    }
}
