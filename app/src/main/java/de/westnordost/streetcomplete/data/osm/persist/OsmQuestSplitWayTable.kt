package de.westnordost.streetcomplete.data.osm.persist

object OsmQuestSplitWayTable {
    const val NAME = "osm_split_ways"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val QUEST_TYPE = "quest_type"
        const val WAY_ID = "way_id"
        const val SPLITS = "splits"
        const val SOURCE = "source"
    }
}
