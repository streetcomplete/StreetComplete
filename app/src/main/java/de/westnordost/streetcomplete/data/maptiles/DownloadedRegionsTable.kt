package de.westnordost.streetcomplete.data.maptiles

object DownloadedRegionsTable {
    const val NAME = "downloaded_regions"

    object Columns {
        const val ID = "id"
        const val DATE = "date"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.DATE} int NOT NULL
        );
    """
}
