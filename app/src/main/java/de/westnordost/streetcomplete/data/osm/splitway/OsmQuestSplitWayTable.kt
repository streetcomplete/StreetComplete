package de.westnordost.streetcomplete.data.osm.splitway

object OsmQuestSplitWayTable {
    const val NAME = "osm_split_ways"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val WAY_ID = "way_id"
        const val SPLITS = "splits"
        const val SOURCE = "source"
        const val QUEST_TYPES_ON_WAY = "quest_types_on_way"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} int PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.WAY_ID} int NOT NULL,
            ${Columns.SPLITS} blob NOT NULL,
            ${Columns.SOURCE} varchar(255) NOT NULL,
            ${Columns.QUEST_TYPES_ON_WAY} text
        );"""
}
