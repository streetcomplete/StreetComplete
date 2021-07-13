package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class QuestTypeOrderController @Inject constructor(
    private val questTypeOrderDao: QuestTypeOrderDao,
    private val questProfilesSource: QuestProfilesSource
): QuestTypeOrderSource {

    private val listeners = CopyOnWriteArrayList<QuestTypeOrderSource.Listener>()

    init {
        questProfilesSource.addListener(object : QuestProfilesSource.Listener {
            override fun onSelectedQuestProfileChanged() {
                onQuestTypeOrderChanged()
            }
            override fun onAddedQuestProfile(profile: QuestProfile) {}
            override fun onDeletedQuestProfile(profileId: Long) {
                questTypeOrderDao.clear(profileId)
            }
        })
    }

    fun addOrderItem(item: QuestType<*>, toAfter: QuestType<*>) {
        questTypeOrderDao.put(questProfilesSource.selectedQuestProfileId, item.name to toAfter.name)
        onQuestTypeOrderAdded(item, toAfter)
    }

    override fun sort(questTypes: MutableList<QuestType<*>>) {
        val orders = questTypeOrderDao.getAll(questProfilesSource.selectedQuestProfileId)
        for ((item, toAfter) in orders) {
            val itemIndex = questTypes.indexOfFirst { it.name == item }
            val toAfterIndex = questTypes.indexOfFirst { it.name == toAfter }
            if (itemIndex == -1 || toAfterIndex == -1) continue

            val questType = questTypes.removeAt(itemIndex)
            questTypes.add(toAfterIndex + if (itemIndex > toAfterIndex) 1 else 0, questType)
        }
    }

    fun clear() {
        questTypeOrderDao.clear(questProfilesSource.selectedQuestProfileId)
        onQuestTypeOrderChanged()
    }

    override fun addListener(listener: QuestTypeOrderSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestTypeOrderSource.Listener) {
        listeners.remove(listener)
    }
    private fun onQuestTypeOrderAdded(item: QuestType<*>, toAfter: QuestType<*>) {
        listeners.forEach { it.onQuestTypeOrderAdded(item, toAfter) }
    }
    private fun onQuestTypeOrderChanged() {
        listeners.forEach { it.onQuestTypeOrdersChanged() }
    }
}

private val QuestType<*>.name get() = this::class.simpleName!!
