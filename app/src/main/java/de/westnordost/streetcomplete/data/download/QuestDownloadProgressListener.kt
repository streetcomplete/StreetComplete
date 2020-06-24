package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.quest.QuestType

interface QuestDownloadProgressListener {
    fun onStarted() {}
    fun onStarted(questType: QuestType<*>) {}
    fun onFinished(questType: QuestType<*>) {}
    fun onError(e: Exception) {}
    fun onFinished() {}
    fun onSuccess() {}
}
