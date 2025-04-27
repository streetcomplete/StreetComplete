package de.westnordost.streetcomplete.data.presets

interface EditTypePresetsSource {
    interface Listener {
        fun onSelectionChanged()
        fun onAdded(preset: EditTypePreset)
        fun onRenamed(preset: EditTypePreset)
        fun onDeleted(presetId: Long)
    }

    val selectedId: Long
    val selectedEditTypePresetName: String?

    /** get the name of the preset with the given [presetId] or null if it does not exist */
    fun getName(presetId: Long): String?
    /** get all edit type presets */
    fun getAll(): List<EditTypePreset>
    /** find a edit type preset by name, returns null if none by that name exists */
    fun getByName(name: String): EditTypePreset?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
