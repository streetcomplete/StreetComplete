package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import java.util.concurrent.CopyOnWriteArrayList

class QuestTypeOrderController(
    private val questTypeOrderDao: QuestTypeOrderDao,
    private val questPresetsSource: QuestPresetsSource
) : QuestTypeOrderSource {

    private val listeners = CopyOnWriteArrayList<QuestTypeOrderSource.Listener>()

    init {
        questPresetsSource.addListener(object : QuestPresetsSource.Listener {
            override fun onSelectedQuestPresetChanged() {
                onQuestTypeOrderChanged()
            }
            override fun onAddedQuestPreset(preset: QuestPreset) {}
            override fun onRenamedQuestPreset(preset: QuestPreset) {}
            override fun onDeletedQuestPreset(presetId: Long) {
                questTypeOrderDao.clear(presetId)
            }
        })
    }

    fun addOrderItem(item: QuestType, toAfter: QuestType) {
        questTypeOrderDao.put(questPresetsSource.selectedId, item.name to toAfter.name)
        onQuestTypeOrderAdded(item, toAfter)
    }

    override fun sort(questTypes: MutableList<QuestType>) {
        val orders = questTypeOrderDao.getAll(questPresetsSource.selectedId)
        for ((item, toAfter) in orders) {
            val itemIndex = questTypes.indexOfFirst { it.name == item }
            val toAfterIndex = questTypes.indexOfFirst { it.name == toAfter }
            if (itemIndex == -1 || toAfterIndex == -1) continue

            val questType = questTypes.removeAt(itemIndex)
            questTypes.add(toAfterIndex + if (itemIndex > toAfterIndex) 1 else 0, questType)
        }
    }

    fun clear() {
        questTypeOrderDao.clear(questPresetsSource.selectedId)
        onQuestTypeOrderChanged()
    }

    override fun addListener(listener: QuestTypeOrderSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestTypeOrderSource.Listener) {
        listeners.remove(listener)
    }
    private fun onQuestTypeOrderAdded(item: QuestType, toAfter: QuestType) {
        listeners.forEach { it.onQuestTypeOrderAdded(item, toAfter) }
    }
    private fun onQuestTypeOrderChanged() {
        listeners.forEach { it.onQuestTypeOrdersChanged() }
    }
}
