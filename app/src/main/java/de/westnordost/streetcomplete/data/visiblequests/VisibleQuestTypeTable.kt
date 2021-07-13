package de.westnordost.streetcomplete.data.visiblequests

object VisibleQuestTypeTable {
    const val NAME = "quest_visibility"

    object Columns {
        const val QUEST_PRESET_ID = "quest_preset_id"
        const val QUEST_TYPE = "quest_type"
        const val VISIBILITY = "visibility"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_PRESET_ID} INTEGER NOT NULL,
            ${Columns.QUEST_TYPE} TEXT,
            ${Columns.VISIBILITY} INTEGER NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (${Columns.QUEST_PRESET_ID}, ${Columns.QUEST_TYPE})
        );"""
}
