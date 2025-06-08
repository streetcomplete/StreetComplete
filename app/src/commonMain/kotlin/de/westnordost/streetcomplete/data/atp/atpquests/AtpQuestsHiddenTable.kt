package de.westnordost.streetcomplete.data.atp

object AtpQuestsHiddenTable {
    const val NAME = "atp_entries_hidden"

    object Columns {
        const val ATP_ENTRY_ID = "atp_id"
        const val TIMESTAMP = "timestamp"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ATP_ENTRY_ID} INTEGER PRIMARY KEY,
            ${Columns.TIMESTAMP} int NOT NULL
        );
    """
}
