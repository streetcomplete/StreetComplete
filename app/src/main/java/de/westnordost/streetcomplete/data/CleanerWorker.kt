package de.westnordost.streetcomplete.data

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.westnordost.streetcomplete.Injector
import javax.inject.Inject

class CleanerWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    @Inject internal lateinit var cleaner: Cleaner

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun doWork(): Result {
        cleaner.clean()
        return Result.success()
    }
}
