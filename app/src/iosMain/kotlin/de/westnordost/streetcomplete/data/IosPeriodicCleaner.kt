package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.util.ktx.pendingTaskRequests
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSinceNow

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosPeriodicCleaner(private val clean: () -> Job) : PeriodicCleaner {
    private val scheduler = BGTaskScheduler.sharedScheduler
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineName("Cleaner"))
    private val enqueueMutex = Mutex()

    /** Must be called once during app launch, including launches for background work. */
    fun register() {
        check(scheduler.registerForTaskWithIdentifier(TASK_ID, null) { task ->
            if (task != null) run(task)
        }) { "Background cleanup identifier is missing from BGTaskSchedulerPermittedIdentifiers" }
    }

    override fun enqueue() {
        scope.launch {
            enqueueMutex.withLock {
                // Submitting again replaces the request and would postpone cleanup on every visit.
                if (scheduler.pendingTaskRequests().any { it.identifier == TASK_ID }) return@withLock

                val request = BGProcessingTaskRequest(TASK_ID).apply {
                    earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(24 * 60 * 60.0)
                }
                memScoped {
                    val error = alloc<ObjCObjectVar<NSError?>>()
                    if (!scheduler.submitTaskRequest(request, error.ptr)) {
                        Log.w(TAG, "Unable to schedule background cleanup: ${error.value?.localizedDescription}")
                    }
                }
            }
        }
    }

    private fun run(task: BGTask) {
        val work = clean()
        task.expirationHandler = { work.cancel() }
        work.invokeOnCompletion { cause -> task.setTaskCompletedWithSuccess(cause == null) }
    }

    fun close() { scope.cancel() }

    private companion object {
        const val TAG = "Cleaner"
        const val TASK_ID = "app.streetcomplete.cleanup"
    }
}
