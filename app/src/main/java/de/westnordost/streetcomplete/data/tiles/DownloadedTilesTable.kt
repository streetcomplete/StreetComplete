package de.westnordost.streetcomplete.data.tiles

object DownloadedTilesTable {
    const val NAME = "downloaded_tiles"

    object Columns {
        const val X = "x"
        const val Y = "y"
        const val QUEST_TYPE = "quest_type"
        const val DATE = "date"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.X} int NOT NULL, 
            ${Columns.Y} int NOT NULL, 
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL, 
            ${Columns.DATE} int NOT NULL, 
            CONSTRAINT primary_key PRIMARY KEY (${Columns.X}, ${Columns.Y}, ${Columns.QUEST_TYPE})
        );"""
}
