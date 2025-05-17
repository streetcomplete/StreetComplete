package de.westnordost.streetcomplete.data.user.statistics

object CountryStatisticsTables {
    const val NAME = "country_statistics"
    const val NAME_CURRENT_WEEK = "country_statistics_current_week"

    object Columns {
        const val COUNTRY_CODE = "country_code"
        const val SUCCEEDED = "succeeded"
        const val RANK = "rank"
    }

    fun create(name: String) = """
        CREATE TABLE $name (
            ${Columns.COUNTRY_CODE} varchar(255) PRIMARY KEY,
            ${Columns.SUCCEEDED} int NOT NULL,
            ${Columns.RANK} int
        );
    """
}
