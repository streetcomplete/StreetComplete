package de.westnordost.streetcomplete.data.visiblequests

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.Listeners

/** Controls the ids and names of quest presets */
class QuestPresetsController(
    private val questPresetsDao: QuestPresetsDao,
    private val prefs: Preferences
) : QuestPresetsSource {

    private val listeners = Listeners<QuestPresetsSource.Listener>()

    // must have local reference because the listeners are only a weak reference
    private val settingsListener: SettingsListener = prefs.onSelectedQuestPresetChanged {
        onSelectedQuestPresetChanged()
    }

    override var selectedId: Long
        get() = prefs.selectedQuestPreset
        set(value) { prefs.selectedQuestPreset = value }

    override val selectedQuestPresetName: String? get() =
        questPresetsDao.getName(selectedId)

    fun add(presetName: String): Long {
        val presetId = questPresetsDao.add(presetName)
        onAddedQuestPreset(presetId, presetName)
        return presetId
    }

    override fun getName(presetId: Long): String? =
        questPresetsDao.getName(presetId)

    fun rename(presetId: Long, name: String) {
        questPresetsDao.rename(presetId, name)
        onRenamedQuestPreset(presetId, name)
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

    override fun getByName(name: String): QuestPreset? =
        questPresetsDao.getAll().find { it.name == name }

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
    private fun onRenamedQuestPreset(presetId: Long, presetName: String) {
        listeners.forEach { it.onRenamedQuestPreset(QuestPreset(presetId, presetName)) }
    }
    private fun onDeletedQuestPreset(presetId: Long) {
        listeners.forEach { it.onDeletedQuestPreset(presetId) }
    }
}
