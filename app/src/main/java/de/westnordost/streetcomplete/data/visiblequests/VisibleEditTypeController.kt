package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.AllEditTypes
import de.westnordost.streetcomplete.data.osm.edits.EditType
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.presets.EditTypePreset
import de.westnordost.streetcomplete.data.presets.EditTypePresetsSource
import de.westnordost.streetcomplete.util.Listeners

/** Controller to set/get edit types as enabled or disabled. This controls only the visibility
 *  of edit types per user preference and does not take anything else into account that may
 *  make an edit type invisible (overlays, ...) */
class VisibleEditTypeController(
    private val visibleEditTypeDao: VisibleEditTypeDao,
    private val editTypePresetsSource: EditTypePresetsSource,
    private val allEditTypes: AllEditTypes,
) : VisibleEditTypeSource {

    private val listeners = Listeners<VisibleEditTypeSource.Listener>()

    init {
        editTypePresetsSource.addListener(object : EditTypePresetsSource.Listener {
            override fun onSelectionChanged() {
                _visibleEditTypes = null
                onVisibilitiesChanged()
            }
            override fun onAdded(preset: EditTypePreset) {}
            override fun onRenamed(preset: EditTypePreset) {}
            override fun onDeleted(presetId: Long) {
                clearVisibilities(allEditTypes, presetId)
            }
        })
    }

    /** in-memory cache of visible edit types */
    private var _visibleEditTypes: MutableMap<String, Boolean>? = null
    private val visibleEditTypes: MutableMap<String, Boolean>
        get() = synchronized(this) {
            if (_visibleEditTypes == null) {
                _visibleEditTypes = visibleEditTypeDao.getAll(selectedPresetId)
            }
            return _visibleEditTypes!!
        }

    private val selectedPresetId: Long get() = editTypePresetsSource.selectedId

    fun copyVisibilities(presetId: Long, newPresetId: Long) {
        visibleEditTypeDao.putAll(newPresetId, visibleEditTypeDao.getAll(presetId))
        if (newPresetId == selectedPresetId) onVisibilitiesChanged()
    }

    fun setVisibility(editType: EditType, visible: Boolean, presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        synchronized(this) {
            visibleEditTypeDao.put(id, editType.name, visible)
            if (id == selectedPresetId) visibleEditTypes[editType.name] = visible
        }
        if (id == selectedPresetId) onVisibilityChanged(editType, visible)
    }

    fun setVisibilities(editTypeVisibilities: Map<EditType, Boolean>, presetId: Long? = null) {
        val editTypeNameVisibilities = editTypeVisibilities
            .filter { it.key !is OsmNoteQuestType }
            .mapKeys { it.key.name }
        val id = presetId ?: selectedPresetId
        synchronized(this) {
            visibleEditTypeDao.putAll(id, editTypeNameVisibilities)
            if (id == selectedPresetId) {
                for ((editTypeName, visible) in editTypeNameVisibilities) {
                    visibleEditTypes[editTypeName] = visible
                }
            }
        }
        if (id == selectedPresetId) onVisibilitiesChanged()
    }

    fun clearVisibilities(editTypes: Collection<EditType>, presetId: Long? = null) {
        val id = presetId ?: selectedPresetId
        synchronized(this) {
            visibleEditTypeDao.clear(id, editTypes.map { it.name })
            if (id == selectedPresetId) {
                editTypes.forEach { visibleEditTypes.remove(it.name) }
            }
        }
        if (id == selectedPresetId) onVisibilitiesChanged()
    }

    override fun isVisible(editType: EditType): Boolean =
        visibleEditTypes[editType.name] ?: (editType.defaultDisabledMessage <= 0)

    override fun getVisible(presetId: Long?): Set<EditType> {
        val visibilities = visibleEditTypeDao.getAll(presetId ?: selectedPresetId)

        return allEditTypes.filter { editType ->
            visibilities[editType.name] ?: (editType.defaultDisabledMessage <= 0)
        }.toSet()
    }

    override fun addListener(listener: VisibleEditTypeSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: VisibleEditTypeSource.Listener) {
        listeners.remove(listener)
    }
    private fun onVisibilityChanged(editType: EditType, visible: Boolean) {
        listeners.forEach { it.onVisibilityChanged(editType, visible) }
    }
    private fun onVisibilitiesChanged() {
        listeners.forEach { it.onVisibilitiesChanged() }
    }
}
