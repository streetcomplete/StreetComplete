package de.westnordost.streetcomplete.data.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield

/**
 * CoroutineIntentService is similar to IntentService only that it is coroutines enabled and
 * has the option to cancel any previous work when a new intent arrives.
 *
 * @see android.app.IntentService
 */
abstract class CoroutineIntentService(name: String) : Service() {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("CoroutineIntentService[$name]")
    )
    private var currentJob: Job? = null
    private val mutex = Mutex()

    /**
     * You should not override this method for your SingleIntentService. Instead,
     * override [.onHandleIntent], which the system calls when the IntentService
     * receives a start request.
     * @see android.app.Service.onStartCommand
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val cancelPreviousIntent = intent?.getBooleanExtra(ARG_PREVIOUS_CANCEL, false) ?: false
        scope.launch {
            mutex.withLock {
                if (cancelPreviousIntent) currentJob?.cancel()
                currentJob?.join()
                currentJob = scope.launch {
                    onHandleIntent(intent)
                    yield()
                    stopSelf(startId)
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
    }

    /**
     * Unless you provide binding for your service, you don't need to implement this
     * method, because the default implementation returns null.
     * @see android.app.Service.onBind
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * This method is invoked on the worker thread with a request to process. The processing
     * happens on a worker thread that runs independently from other application logic.
     * When the request has been handled, the SingleIntentService stops itself, so you should not
     * call [.stopSelf].
     *
     * @param intent The value passed to [android.content.Context.startService].
     * This may be null if the service is being restarted after
     * its process has gone away; see [android.app.Service.onStartCommand]
     * for details.
     */
    @WorkerThread protected abstract suspend fun onHandleIntent(intent: Intent?)

    fun cancel() {
        currentJob?.cancel()
    }

    companion object {
        const val ARG_PREVIOUS_CANCEL = "cancelPrevious"
    }
}
