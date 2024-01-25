package de.westnordost.streetcomplete.data.visiblequests

interface QuestPresetsSource {
    interface Listener {
        fun onSelectedQuestPresetChanged()
        fun onAddedQuestPreset(preset: QuestPreset)
        fun onRenamedQuestPreset(preset: QuestPreset)
        fun onDeletedQuestPreset(presetId: Long)
    }

    val selectedId: Long
    val selectedQuestPresetName: String?

    /** get the name of the preset with the given [presetId] or null if it does not exist */
    fun getName(presetId: Long): String?
    /** get all quest presets */
    fun getAll(): List<QuestPreset>
    /** find a quest preset by name, returns null if none by that name exists */
    fun getByName(name: String): QuestPreset?

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
