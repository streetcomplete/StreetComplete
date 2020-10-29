package de.westnordost.streetcomplete.data.download

import androidx.annotation.DrawableRes

interface DownloadProgressListener {
    fun onStarted() {}
    fun onStarted(item: DownloadItem) {}
    fun onFinished(item: DownloadItem) {}
    fun onError(e: Exception) {}
    fun onFinished() {}
    fun onSuccess() {}
}

data class DownloadItem(@DrawableRes val iconResId: Int, val title: String)