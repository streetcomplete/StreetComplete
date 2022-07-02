package de.westnordost.streetcomplete.data.user.statistics

object EditTypeStatisticsTable {
    const val NAME = "quest_statistics"

    object Columns {
        const val ELEMENT_EDIT_TYPE = "quest_type"
        const val SUCCEEDED = "succeeded"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ELEMENT_EDIT_TYPE} varchar(255) PRIMARY KEY,
            ${Columns.SUCCEEDED} int NOT NULL
        );
    """
}
