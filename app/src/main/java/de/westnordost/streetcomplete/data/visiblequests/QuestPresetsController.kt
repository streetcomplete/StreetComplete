package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
import java.util.concurrent.CopyOnWriteArrayList

class QuestPresetsController(
    private val questPresetsDao: QuestPresetsDao,
    private val selectedQuestPresetStore: SelectedQuestPresetStore,
    private val questTypeOrderDao: QuestTypeOrderDao,
    private val visibleQuestTypeDao: VisibleQuestTypeDao,
    private val prefs: SharedPreferences,
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

    fun add(presetName: String, copyFromId: Long): Long {
        val presetId = questPresetsDao.add(presetName)
        onAddedQuestPreset(presetId, presetName)
        val order = questTypeOrderDao.getAll(copyFromId)
        order.forEach { questTypeOrderDao.put(presetId, it) }
        val visibilities = visibleQuestTypeDao.getAll(copyFromId)
        visibilities.forEach { visibleQuestTypeDao.put(presetId, it.key, it.value) }

        val copyFromQuestSettings = prefs.all.filterKeys { it.startsWith("${copyFromId}_qs_") }
        copyFromQuestSettings.forEach { (key, value) ->
            val newKey = key.replace("${copyFromId}_qs_", "${presetId}_qs_")
            when (value) {
                is Boolean -> prefs.edit().putBoolean(newKey, value).apply()
                is Int -> prefs.edit().putInt(newKey, value).apply()
                is String -> prefs.edit().putString(newKey, value).apply()
                is Long -> prefs.edit().putLong(newKey, value).apply()
                is Float -> prefs.edit().putFloat(newKey, value).apply()
            }
        }
        return presetId
    }

    fun delete(presetId: Long) {
        if (presetId == selectedId) {
            selectedId = 0
        }
        questPresetsDao.delete(presetId)
        val presetSettings = prefs.all.keys.filter { it.startsWith("${presetId}_qs_") }
        presetSettings.forEach { prefs.edit().remove(it).apply() }
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
