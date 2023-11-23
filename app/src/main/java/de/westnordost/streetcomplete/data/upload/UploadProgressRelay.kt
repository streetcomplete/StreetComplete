package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.util.Listeners

class UploadProgressRelay : UploadProgressListener {
    private val listeners = Listeners<UploadProgressListener>()

    override fun onStarted() {
        listeners.forEach { it.onStarted() }
    }
    override fun onProgress(success: Boolean) {
        listeners.forEach { it.onProgress(success) }
    }
    override fun onError(e: Exception) {
        listeners.forEach { it.onError(e) }
    }
    override fun onFinished() {
        listeners.forEach { it.onFinished() }
    }

    fun addListener(listener: UploadProgressListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UploadProgressListener) {
        listeners.remove(listener)
    }
}
