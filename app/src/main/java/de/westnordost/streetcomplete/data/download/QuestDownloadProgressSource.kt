package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.quest.QuestType

interface QuestDownloadProgressSource {
    val isPriorityDownloadInProgress: Boolean
    val isDownloadInProgress: Boolean
    val currentDownloadingQuestType: QuestType<*>?

    fun addQuestDownloadProgressListener(listener: QuestDownloadProgressListener)
    fun removeQuestDownloadProgressListener(listener: QuestDownloadProgressListener)
}