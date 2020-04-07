package de.westnordost.streetcomplete.data.download

import java.util.concurrent.CopyOnWriteArrayList

class QuestDownloadProgressRelay : QuestDownloadProgressListener {

    private val listeners = CopyOnWriteArrayList<QuestDownloadProgressListener>()
    override fun onStarted() {
        listeners.forEach { it.onStarted() }
    }
    override fun onProgress(progress: Float) {
        listeners.forEach { it.onProgress(progress) }
    }
    override fun onError(e: Exception) {
        listeners.forEach { it.onError(e) }
    }
    override fun onSuccess() {
        listeners.forEach { it.onSuccess() }
    }
    override fun onFinished() {
        listeners.forEach { it.onFinished() }
    }
    override fun onNotStarted() {
        listeners.forEach { it.onNotStarted() }
    }

    fun addListener(listener: QuestDownloadProgressListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: QuestDownloadProgressListener) {
        listeners.remove(listener)
    }
}
