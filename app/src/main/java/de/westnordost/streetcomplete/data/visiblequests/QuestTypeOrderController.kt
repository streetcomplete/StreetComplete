package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.Listeners

/** Controls which quest types have been reordered after which other quest types by the user */
class QuestTypeOrderController(
    private val questTypeOrderDao: QuestTypeOrderDao,
    private val questPresetsSource: QuestPresetsSource,
    private val questTypeRegistry: QuestTypeRegistry
) : QuestTypeOrderSource {

    private val listeners = Listeners<QuestTypeOrderSource.Listener>()

    private val selectedPresetId: Long get() = questPresetsSource.selectedId

    init {
        questPresetsSource.addListener(object : QuestPresetsSource.Listener {
            override fun onSelectedQuestPresetChanged() {
                onQuestTypeOrderChanged()
            }
            override fun onAddedQuestPreset(preset: QuestPreset) {}
            override fun onRenamedQuestPreset(preset: QuestPreset) {}
            override fun onDeletedQuestPreset(presetId: Long) {
                clear(presetId)
            }
        })
    }

    fun copyOrders(presetId: Long, newPresetId: Long) {
        questTypeOrderDao.setAll(newPresetId, questTypeOrderDao.getAll(presetId))
        if (newPresetId == selectedPresetId) onQuestTypeOrderChanged()
    }

    fun setOrders(orderItems: List<Pair<QuestType, QuestType>>, presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        questTypeOrderDao.setAll(id, orderItems.map { it.first.name to it.second.name })
        if (id == selectedPresetId) onQuestTypeOrderChanged()
    }

    override fun getOrders(presetId: Long?): List<Pair<QuestType, QuestType>> {
        val id = presetId ?: selectedPresetId

        return questTypeOrderDao.getAll(id).mapNotNull {
            val first = questTypeRegistry.getByName(it.first)
            val second = questTypeRegistry.getByName(it.second)
            if (first != null && second != null) first to second else null
        }
    }

    fun addOrderItem(item: QuestType, toAfter: QuestType, presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        questTypeOrderDao.put(id, item.name to toAfter.name)
        if (id == selectedPresetId) onQuestTypeOrderAdded(item, toAfter)
    }

    override fun sort(questTypes: MutableList<QuestType>, presetId: Long?) {
        val id = presetId ?: selectedPresetId
        val orders = questTypeOrderDao.getAll(id)
        for ((item, toAfter) in orders) {
            val itemIndex = questTypes.indexOfFirst { it.name == item }
            val toAfterIndex = questTypes.indexOfFirst { it.name == toAfter }
            if (itemIndex == -1 || toAfterIndex == -1) continue

            val questType = questTypes.removeAt(itemIndex)
            questTypes.add(toAfterIndex + if (itemIndex > toAfterIndex) 1 else 0, questType)
        }
    }

    fun clear(presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        questTypeOrderDao.clear(id)
        if (id == selectedPresetId) onQuestTypeOrderChanged()
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
