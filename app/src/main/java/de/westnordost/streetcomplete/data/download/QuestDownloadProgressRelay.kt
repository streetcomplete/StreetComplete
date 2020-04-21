package de.westnordost.streetcomplete.data.download

import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.concurrent.CopyOnWriteArrayList

class QuestDownloadProgressRelay : QuestDownloadProgressListener {

    private val listeners = CopyOnWriteArrayList<QuestDownloadProgressListener>()

    override fun onStarted() { listeners.forEach { it.onStarted() } }
    override fun onError(e: Exception) { listeners.forEach { it.onError(e) } }
    override fun onSuccess() { listeners.forEach { it.onSuccess() } }
    override fun onFinished() { listeners.forEach { it.onFinished() } }
    override fun onStarted(questType: QuestType<*>) { listeners.forEach { it.onStarted(questType) } }
    override fun onFinished(questType: QuestType<*>) {listeners.forEach { it.onFinished(questType) } }

    fun addListener(listener: QuestDownloadProgressListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: QuestDownloadProgressListener) {
        listeners.remove(listener)
    }
}
