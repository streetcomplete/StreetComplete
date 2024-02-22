package de.westnordost.streetcomplete.data.download

interface DownloadProgressSource {
    interface Listener {
        fun onStarted() {}
        fun onError(e: Exception) {}
        fun onFinished() {}
        fun onSuccess() {}
    }

    /** @return true if a download triggered by the user is running */
    val isUserInitiatedDownloadInProgress: Boolean
    /** @return true if a download is running */
    val isDownloadInProgress: Boolean

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
