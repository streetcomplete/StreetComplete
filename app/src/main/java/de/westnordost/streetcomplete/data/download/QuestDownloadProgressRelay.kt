package de.westnordost.streetcomplete.data.download

/** Threadsafe relay for QuestDownloadProgressListener. Also, it can show a notification with
 * progress. See startForeground/stopForeground
 *
 * (setting the listener and calling the listener methods can safely be done from different threads)  */
class QuestDownloadProgressRelay(private val notification: QuestDownloadNotification)
    : QuestDownloadProgressListener {

    var listener: QuestDownloadProgressListener? = null
        set(listener) {
            field = listener
            // bring listener up-to-date
            if (listener != null) {
                if (isInProgress) {
                    listener.onStarted()
                    progress?.let { listener.onProgress(it) }
                } else {
                    tryDispatchError()
                }
            }
        }

    // state
    private var isInProgress: Boolean = false
    private var error: Exception? = null
    private var progress: Float? = null
    private var showNotification = false

    fun startForeground() {
        showNotification = true
        if (isInProgress) {
            notification.showProgress(progress ?: 0f)
        } else {
            notification.hide()
        }
    }

    fun stopForeground() {
        showNotification = false
        notification.hide()
    }

    override fun onStarted() {
        isInProgress = true
        if (showNotification) notification.showProgress(0f)
        listener?.onStarted()
    }

    override fun onNotStarted() {
        listener?.onNotStarted()
    }

    override fun onProgress(progress: Float) {
        this.progress = progress
        if (showNotification) notification.showProgress(progress)
        listener?.onProgress(progress)
    }

    override fun onError(e: Exception) {
        error = e
        tryDispatchError()
    }

    override fun onSuccess() {
        listener?.onSuccess()
    }

    override fun onFinished() {
        isInProgress = false
        progress = null
        if (showNotification) notification.hide()
        listener?.onFinished()
    }

    private fun tryDispatchError() {
        val listener = listener
        if (listener != null) {
            val error = error
            if (error != null) {
                listener.onError(error)
                this.error = null
            }
        }
    }
}
