package de.westnordost.streetcomplete.data.user

object CountryStatisticsTable {
    const val NAME = "country_statistics"

    object Columns {
        const val COUNTRY_CODE = "country_code"
        const val SUCCEEDED = "succeeded"
        const val RANK = "rank"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.COUNTRY_CODE} varchar(255) PRIMARY KEY,
            ${Columns.SUCCEEDED} int NOT NULL,
            ${Columns.RANK} int
        );"""
}
