package de.westnordost.streetcomplete.data.osm.changes

object OsmElementChangesTable {
    const val NAME = "osm_element_changes"

    object Columns {
        const val ID = "id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val SOURCE = "source"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CREATED_TIMESTAMP = "created"
        const val IS_SYNCED = "synced"
        const val TYPE = "type"
        const val CHANGES = "changes"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.SOURCE} varchar(255) NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.CREATED_TIMESTAMP} int NOT NULL,
            ${Columns.IS_SYNCED} int NOT NULL,
            ${Columns.TYPE} varchar(255),
            ${Columns.CHANGES} blob
        );"""
}
