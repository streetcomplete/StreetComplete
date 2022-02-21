package de.westnordost.streetcomplete.data.visiblequests

import java.util.concurrent.CopyOnWriteArrayList

class QuestPresetsController(
    private val questPresetsDao: QuestPresetsDao,
    private val selectedQuestPresetStore: SelectedQuestPresetStore
) : QuestPresetsSource {

    private val listeners = CopyOnWriteArrayList<QuestPresetsSource.Listener>()

    override var selectedId: Long
        get() = selectedQuestPresetStore.get()
        set(value) {
            selectedQuestPresetStore.set(value)
            onSelectedQuestPresetChanged()
        }

    override val selectedQuestPresetName: String? get() =
        questPresetsDao.getName(selectedId)

    fun add(presetName: String): Long {
        val presetId = questPresetsDao.add(presetName)
        onAddedQuestPreset(presetId, presetName)
        return presetId
    }

    fun delete(presetId: Long) {
        if (presetId == selectedId) {
            selectedId = 0
        }
        questPresetsDao.delete(presetId)
        onDeletedQuestPreset(presetId)
    }

    override fun getAll(): List<QuestPreset> =
        questPresetsDao.getAll()

    /* listeners */

    override fun addListener(listener: QuestPresetsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestPresetsSource.Listener) {
        listeners.remove(listener)
    }
    private fun onSelectedQuestPresetChanged() {
        listeners.forEach { it.onSelectedQuestPresetChanged() }
    }
    private fun onAddedQuestPreset(presetId: Long, presetName: String) {
        listeners.forEach { it.onAddedQuestPreset(QuestPreset(presetId, presetName)) }
    }
    private fun onDeletedQuestPreset(presetId: Long) {
        listeners.forEach { it.onDeletedQuestPreset(presetId) }
    }
}
