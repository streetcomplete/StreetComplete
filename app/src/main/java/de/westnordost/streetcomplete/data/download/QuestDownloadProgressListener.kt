package de.westnordost.streetcomplete.data.download

interface QuestDownloadProgressListener {
    fun onStarted()
    fun onProgress(progress: Float)
    fun onError(e: Exception)
    fun onSuccess()
    fun onFinished()
    fun onNotStarted()
}
