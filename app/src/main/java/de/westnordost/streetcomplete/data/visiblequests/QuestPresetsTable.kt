package de.westnordost.streetcomplete.data.visiblequests

object QuestPresetsTable {
    const val NAME = "quest_presets"

    object Columns {
        const val QUEST_PRESET_ID = "quest_preset_id"
        const val QUEST_PRESET_NAME = "quest_preset_name"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_PRESET_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_PRESET_NAME} TEXT NOT NULL
        );"""
}
