package de.westnordost.streetcomplete.data.upload

interface UploadProgressSource {
    interface Listener {
        fun onStarted() {}
        fun onProgress(success: Boolean) {}
        fun onError(e: Exception) {}
        fun onFinished() {}
    }

    val isUploadInProgress: Boolean

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
