package de.westnordost.streetcomplete.data.upload

import java.util.concurrent.CopyOnWriteArrayList

class UploadProgressRelay : UploadProgressListener {
    private val listeners = CopyOnWriteArrayList<UploadProgressListener>()

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