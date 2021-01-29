package de.westnordost.streetcomplete.data.osm.mapdata

object WayTable {
    const val NAME = "osm_ways"

    object Columns {
        const val ID = "id"
        const val VERSION = "version"
        const val TAGS = "tags"
        const val NODE_IDS = "node_ids"
        const val LAST_UPDATE = "last_update"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.VERSION} int NOT NULL,
            ${Columns.TAGS} blob,
            ${Columns.NODE_IDS} blob NOT NULL,
            ${Columns.LAST_UPDATE} int NOT NULL
        );"""
}
