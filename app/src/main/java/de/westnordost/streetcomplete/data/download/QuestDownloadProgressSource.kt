package de.westnordost.streetcomplete.data.download

interface QuestDownloadProgressSource {
    val isPriorityDownloadInProgress: Boolean
    val isDownloadInProgress: Boolean
    val downloadProgress: Float

    fun addQuestDownloadProgressListener(listener: QuestDownloadProgressListener)
    fun removeQuestDownloadProgressListener(listener: QuestDownloadProgressListener)
}