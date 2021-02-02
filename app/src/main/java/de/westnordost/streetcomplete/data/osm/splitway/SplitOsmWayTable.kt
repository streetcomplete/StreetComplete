package de.westnordost.streetcomplete.data.osm.splitway

object SplitOsmWayTable {
    const val NAME = "osm_split_ways"

    object Columns {
        const val ID = "id"
        const val QUEST_TYPE = "quest_type"
        const val WAY_ID = "way_id"
        const val SPLITS = "splits"
        const val SOURCE = "source"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.WAY_ID} int NOT NULL,
            ${Columns.SPLITS} blob NOT NULL,
            ${Columns.SOURCE} varchar(255) NOT NULL
        );"""
}
