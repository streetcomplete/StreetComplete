package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.util.Listeners

class DownloadProgressRelay : DownloadProgressListener {

    private val listeners = Listeners<DownloadProgressListener>()

    override fun onStarted() { listeners.forEach { it.onStarted() } }
    override fun onError(e: Exception) { listeners.forEach { it.onError(e) } }
    override fun onSuccess() { listeners.forEach { it.onSuccess() } }
    override fun onFinished() { listeners.forEach { it.onFinished() } }

    fun addListener(listener: DownloadProgressListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: DownloadProgressListener) {
        listeners.remove(listener)
    }
}
