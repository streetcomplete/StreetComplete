package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosPeriodicCleaner(private val clean: suspend () -> Unit) : PeriodicCleaner {
    private val scheduler = BGTaskScheduler.sharedScheduler
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineName("Cleaner"))
    private val mutex = Mutex()
    private var registered = false
    private var job: Job? = null

    /** Must be called once during app launch, including launches for background work. */
    fun register() {
        if (registered) return
        registered = scheduler.registerForTaskWithIdentifier(TASK_ID, dispatch_get_main_queue()) { task ->
            if (task != null) run(task)
        }
        if (!registered) Log.e(TAG, "Unable to register background cleanup")
    }

    override fun enqueue() {
        scope.launch {
            mutex.withLock {
                if (!registered || job?.isCompleted == false) return@withLock
                val pending = suspendCancellableCoroutine { continuation ->
                    scheduler.getPendingTaskRequestsWithCompletionHandler { requests ->
                        continuation.resume(requests.orEmpty())
                    }
                }
                // Submitting again replaces the request and would postpone cleanup on every visit.
                if (pending.any { (it as BGTaskRequest).identifier == TASK_ID }) return@withLock
                if (job?.isCompleted == false) return@withLock

                val request = BGProcessingTaskRequest(TASK_ID).apply {
                    earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(24 * 60 * 60.0)
                    requiresNetworkConnectivity = false
                    requiresExternalPower = false
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
        if (job?.isCompleted == false) {
            task.setTaskCompletedWithSuccess(false)
            return
        }
        val completed = atomic(false)
        fun complete(success: Boolean) {
            if (completed.compareAndSet(expect = false, update = true)) {
                task.setTaskCompletedWithSuccess(success)
            }
        }
        val work = scope.launch(start = CoroutineStart.LAZY) {
            try {
                clean()
                complete(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unable to clean old data", e)
            }
        }
        work.invokeOnCompletion { complete(false) }
        job = work
        task.expirationHandler = {
            work.cancel()
            // A synchronous database batch may still be finishing; do not block the expiration handler.
            complete(false)
        }
        work.start()
    }

    fun close() { scope.cancel() }

    private companion object {
        const val TAG = "Cleaner"
        const val TASK_ID = "app.streetcomplete.cleanup"
    }
}
