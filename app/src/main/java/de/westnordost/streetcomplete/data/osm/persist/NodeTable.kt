package de.westnordost.streetcomplete.data.osm.persist

object NodeTable {
    const val NAME = "osm_nodes"

    object Columns {
        const val ID = "id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val VERSION = "version"
        const val TAGS = "tags"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.VERSION} int NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.TAGS} blob
        );"""
}
