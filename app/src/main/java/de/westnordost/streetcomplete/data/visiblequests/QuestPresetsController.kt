package de.westnordost.streetcomplete.data.visiblequests

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class QuestPresetsController @Inject constructor(
    private val questPresetsDao: QuestPresetsDao,
    private val selectedQuestPresetStore: SelectedQuestPresetStore
): QuestPresetsSource {

    private val listeners = CopyOnWriteArrayList<QuestPresetsSource.Listener>()

    override var selectedQuestPresetId: Long
        get() = selectedQuestPresetStore.get()
        set(value) {
            selectedQuestPresetStore.set(value)
            onSelectedQuestProfileChanged()
        }

    override val selectedQuestPresetName: String? get() =
        questPresetsDao.getName(selectedQuestPresetId)

    fun addQuestProfile(presetName: String): Long {
        val presetId = questPresetsDao.add(presetName)
        onAddedQuestProfile(presetId, presetName)
        return presetId
    }

    fun deleteQuestPreset(presetId: Long) {
        if (presetId == selectedQuestPresetId) {
            selectedQuestPresetId = 0
        }
        questPresetsDao.delete(presetId)
        onDeletedQuestProfile(presetId)
    }

    override fun getAllQuestPresets(): List<QuestPreset> =
        questPresetsDao.getAll()

    /* listeners */

    override fun addListener(listener: QuestPresetsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestPresetsSource.Listener) {
        listeners.remove(listener)
    }
    private fun onSelectedQuestProfileChanged() {
        listeners.forEach { it.onSelectedQuestPresetChanged() }
    }
    private fun onAddedQuestProfile(presetId: Long, presetName: String) {
        listeners.forEach { it.onAddedQuestPreset(QuestPreset(presetId, presetName)) }
    }
    private fun onDeletedQuestProfile(presetId: Long) {
        listeners.forEach { it.onDeletedQuestPreset(presetId) }
    }
}
