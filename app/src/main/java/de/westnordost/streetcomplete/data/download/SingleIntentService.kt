package de.westnordost.streetcomplete.data.download

import android.app.Service
import android.content.Intent
import android.os.*
import androidx.annotation.WorkerThread
import java.util.concurrent.atomic.AtomicBoolean

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

    @Volatile private lateinit var serviceLooper: Looper
    @Volatile private lateinit var serviceHandler: ServiceHandler
    private var cancelState: AtomicBoolean = AtomicBoolean(false)

    private inner class ServiceHandler(looper: Looper?) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            cancelState = AtomicBoolean(false)
            onHandleIntent(msg.obj as Intent, cancelState)
            stopSelf(msg.arg1)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("SingleIntentService[$name]")
        thread.start()
        serviceLooper = thread.looper
        serviceHandler = ServiceHandler(serviceLooper)
    }

    override fun onStart(intent: Intent?, startId: Int) {
        cancel()
        val msg = serviceHandler.obtainMessage()
        msg.arg1 = startId
        msg.obj = intent
        serviceHandler.sendMessage(msg)
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
        cancel()
        serviceLooper.quit()
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
     * happens on a worker thread that runs independently from other  application logic.
     * When the request has been handled, the SingleIntentService stops itself, so you should not
     * call [.stopSelf].
     *
     * @param intent The value passed to [android.content.Context.startService].
     * This may be null if the service is being restarted after
     * its process has gone away; see [android.app.Service.onStartCommand]
     * for details.
     */
    @WorkerThread protected abstract fun onHandleIntent(intent: Intent?, cancelState: AtomicBoolean)

    fun cancel() {
        serviceHandler.removeCallbacksAndMessages(null)
        cancelState.set(true)
    }
}
