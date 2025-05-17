package de.westnordost.streetcomplete.data.download.tiles

object DownloadedTilesTable {
    const val NAME = "downloaded_tiles"

    object Columns {
        const val X = "x"
        const val Y = "y"
        const val DATE = "date"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.X} int NOT NULL,
            ${Columns.Y} int NOT NULL,
            ${Columns.DATE} int NOT NULL,
            CONSTRAINT primary_key PRIMARY KEY (${Columns.X}, ${Columns.Y})
        );
    """
}
