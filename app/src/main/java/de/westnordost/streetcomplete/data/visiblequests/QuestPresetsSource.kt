package de.westnordost.streetcomplete.data.visiblequests

interface QuestPresetsSource {
    interface Listener {
        fun onSelectedQuestPresetChanged()
        fun onAddedQuestPreset(preset: QuestPreset)
        fun onDeletedQuestPreset(presetId: Long)
    }

    val selectedQuestPresetId: Long
    val selectedQuestPresetName: String?

    fun getAllQuestPresets(): List<QuestPreset>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
