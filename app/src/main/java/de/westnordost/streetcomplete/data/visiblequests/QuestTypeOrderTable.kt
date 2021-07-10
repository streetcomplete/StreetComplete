package de.westnordost.streetcomplete.data.visiblequests

object QuestTypeOrderTable {
    const val NAME = "quest_order"

    object Columns {
        const val QUEST_PROFILE_ID = "quest_profile_id"
        const val BEFORE = "before"
        const val AFTER = "after"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_PROFILE_ID} INTEGER NOT NULL,
            ${Columns.BEFORE} TEXT NOT NULL,
            ${Columns.AFTER} TEXT NOT NULL
        );"""

    const val INDEX_CREATE = "CREATE INDEX quest_order_idx ON $NAME (${Columns.QUEST_PROFILE_ID});"
}
