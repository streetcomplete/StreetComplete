package de.westnordost.streetcomplete.data.upload

interface QuestChangesUploadProgressListener {
    fun onStarted()
    fun onProgress(success: Boolean)
    fun onError(e: Exception)
    fun onFinished()
}
