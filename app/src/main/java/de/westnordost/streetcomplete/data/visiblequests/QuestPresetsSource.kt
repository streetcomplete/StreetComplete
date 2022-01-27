package de.westnordost.streetcomplete.data.visiblequests

interface QuestPresetsSource {
    interface Listener {
        fun onSelectedQuestPresetChanged()
        fun onAddedQuestPreset(preset: QuestPreset)
        fun onDeletedQuestPreset(presetId: Long)
    }

    val selectedId: Long
    val selectedQuestPresetName: String?

    fun getAll(): List<QuestPreset>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
