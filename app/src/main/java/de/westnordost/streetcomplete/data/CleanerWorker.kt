package de.westnordost.streetcomplete.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CleanerWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams), KoinComponent {
    internal val cleaner: Cleaner by inject()

    override fun doWork(): Result {
        cleaner.clean()
        return Result.success()
    }
}
