package de.westnordost.streetcomplete.data.visiblequests

object QuestProfilesTable {
    const val NAME = "quest_profiles"

    object Columns {
        const val QUEST_PROFILE_ID = "quest_profile_id"
        const val QUEST_PROFILE_NAME = "quest_profile_name"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_PROFILE_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_PROFILE_NAME} TEXT NOT NULL
        );"""
}
