package de.westnordost.streetcomplete.data.download

import android.app.Service
import android.content.Intent
import android.os.*
import androidx.annotation.WorkerThread
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * SingleIntentService is similar to IntentService only that it cancels any previous intent when a
 * new arrives.
 *
 * SingleIntentService will receive the Intent, launch a worker thread, and stop the service as
 * appropriate.
 *
 * @see android.app.IntentService
 */
abstract class SingleIntentService(private val name: String) : Service() {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("SingleIntentService[$name]"))
    private var currentJob: Job? = null
    private val mutex = Mutex()

    override fun onStart(intent: Intent?, startId: Int) {
        scope.launch {
            mutex.withLock {
                currentJob?.cancelAndJoin()
                currentJob = scope.launch {
                    onHandleIntent(intent)
                    yield()
                    stopSelf(startId)
                }
            }
        }
    }

    /**
     * You should not override this method for your SingleIntentService. Instead,
     * override [.onHandleIntent], which the system calls when the IntentService
     * receives a start request.
     * @see android.app.Service.onStartCommand
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onStart(intent, startId)
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
}
