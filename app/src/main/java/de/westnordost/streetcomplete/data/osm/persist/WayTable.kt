package de.westnordost.streetcomplete.data.osm.persist

object WayTable {
    const val NAME = "osm_ways"

    object Columns {
        const val ID = "id"
        const val VERSION = "version"
        const val TAGS = "tags"
        const val NODE_IDS = "node_ids"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.VERSION} int NOT NULL,
            ${Columns.TAGS} blob,
            ${Columns.NODE_IDS} blob NOT NULL
        );"""
}
