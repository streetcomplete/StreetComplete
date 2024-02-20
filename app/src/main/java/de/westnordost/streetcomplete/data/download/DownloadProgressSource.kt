package de.westnordost.streetcomplete.data.download

interface DownloadProgressSource {
    interface Listener {
        fun onStarted() {}
        fun onError(e: Exception) {}
        fun onFinished() {}
        fun onSuccess() {}
    }

    val isPriorityDownloadInProgress: Boolean
    val isDownloadInProgress: Boolean

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
