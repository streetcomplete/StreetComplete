package de.westnordost.streetcomplete.data.download

interface DownloadProgressSource {
    val isPriorityDownloadInProgress: Boolean
    val isDownloadInProgress: Boolean
    val currentDownloadItem: DownloadItem?

    fun addDownloadProgressListener(listener: DownloadProgressListener)
    fun removeDownloadProgressListener(listener: DownloadProgressListener)
}