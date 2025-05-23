package de.westnordost.streetcomplete.data.user.statistics

object ActiveDaysTable {
    const val NAME = "active_days_statistics"

    object Columns {
        const val DATE = "date"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.DATE} text PRIMARY KEY
        );
    """
}
