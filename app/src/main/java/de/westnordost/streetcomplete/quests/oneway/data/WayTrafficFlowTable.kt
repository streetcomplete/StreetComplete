package de.westnordost.streetcomplete.quests.oneway.data

object WayTrafficFlowTable {
    const val NAME = "direction_of_flow"

    object Columns {
        const val WAY_ID = "way_id"
        const val IS_FORWARD = "is_forward"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.WAY_ID} int PRIMARY KEY,
            ${Columns.IS_FORWARD} int NOT NULL
        );"""
}
