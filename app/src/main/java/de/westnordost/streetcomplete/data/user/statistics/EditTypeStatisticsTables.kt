package de.westnordost.streetcomplete.data.user.statistics

object EditTypeStatisticsTables {
    const val NAME = "quest_statistics"
    const val NAME_CURRENT_WEEK = "quest_statistics_current_week"

    object Columns {
        const val ELEMENT_EDIT_TYPE = "quest_type"
        const val SUCCEEDED = "succeeded"
    }

    fun create(name: String) = """
        CREATE TABLE $name (
            ${Columns.ELEMENT_EDIT_TYPE} varchar(255) PRIMARY KEY,
            ${Columns.SUCCEEDED} int NOT NULL
        );
    """
}
