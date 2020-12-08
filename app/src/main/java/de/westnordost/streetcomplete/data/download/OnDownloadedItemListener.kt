package de.westnordost.streetcomplete.data.download

interface OnDownloadedItemListener {
    fun onStarted(item: DownloadItem)
    fun onFinished(item: DownloadItem)
}
