package de.westnordost.streetcomplete.data.presets

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.Listeners

/** Controls the ids and names of quest & overlay presets */
class EditTypePresetsController(
    private val editTypePresetsDao: EditTypePresetsDao,
    private val prefs: Preferences
) : EditTypePresetsSource {

    private val listeners = Listeners<EditTypePresetsSource.Listener>()

    // must have local reference because the listeners are only a weak reference
    @Suppress("unused")
    private val settingsListener: SettingsListener = prefs.onSelectedEditTypePresetChanged {
        onSelectionChanged()
    }

    override var selectedId: Long
        get() = prefs.selectedEditTypePreset
        set(value) { prefs.selectedEditTypePreset = value }

    override val selectedEditTypePresetName: String? get() =
        editTypePresetsDao.getName(selectedId)

    fun add(presetName: String): Long {
        val presetId = editTypePresetsDao.add(presetName)
        onAdded(presetId, presetName)
        return presetId
    }

    override fun getName(presetId: Long): String? =
        editTypePresetsDao.getName(presetId)

    fun rename(presetId: Long, name: String) {
        editTypePresetsDao.rename(presetId, name)
        onRenamed(presetId, name)
    }

    fun delete(presetId: Long) {
        if (presetId == selectedId) {
            selectedId = 0
        }
        editTypePresetsDao.delete(presetId)
        val presetSettings = prefs.prefs.keys.filter { it.startsWith("${presetId}_qs_") }
        presetSettings.forEach { prefs.prefs.remove(it) }
        onDeleted(presetId)
    }

    override fun getAll(): List<EditTypePreset> =
        editTypePresetsDao.getAll()

    override fun getByName(name: String): EditTypePreset? =
        editTypePresetsDao.getAll().find { it.name == name }

    /* listeners */

    override fun addListener(listener: EditTypePresetsSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: EditTypePresetsSource.Listener) {
        listeners.remove(listener)
    }
    private fun onSelectionChanged() {
        listeners.forEach { it.onSelectionChanged() }
    }
    private fun onAdded(presetId: Long, presetName: String) {
        listeners.forEach { it.onAdded(EditTypePreset(presetId, presetName)) }
    }
    private fun onRenamed(presetId: Long, presetName: String) {
        listeners.forEach { it.onRenamed(EditTypePreset(presetId, presetName)) }
    }
    private fun onDeleted(presetId: Long) {
        listeners.forEach { it.onDeleted(presetId) }
    }
}
