package de.westnordost.streetcomplete.data.visiblequests

object FavoriteQuestTypeTable {
    const val NAME = "favorite_quests"

    object Columns {
        const val EDIT_TYPE_PRESET_ID = "quest_preset_id"
        const val QUEST = "quest"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.EDIT_TYPE_PRESET_ID} INTEGER NOT NULL,
            ${Columns.QUEST} TEXT NOT NULL,
            PRIMARY KEY (${Columns.EDIT_TYPE_PRESET_ID}, ${Columns.QUEST})
        );
    """
}
