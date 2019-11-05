package de.westnordost.streetcomplete.data.osm.persist

object RelationTable {
    const val NAME = "osm_relations"

    object Columns {
        const val ID = "id"
        const val VERSION = "version"
        const val TAGS = "tags"
        const val MEMBERS = "members"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.VERSION} int NOT NULL,
            ${Columns.TAGS} blob,
            ${Columns.MEMBERS} blob NOT NULL
        );"""
}
