package de.westnordost.streetcomplete.data.osm.edits

object ElementEditsTable {
    const val NAME = "osm_element_edits"

    object Columns {
        const val ID = "id"
        const val QUEST_TYPE = "quest_type"
        const val ELEMENT_ID = "element_id"
        const val ELEMENT_TYPE = "element_type"
        const val ELEMENT = "element"
        const val GEOMETRY = "geometry"
        const val SOURCE = "source"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CREATED_TIMESTAMP = "created"
        const val IS_SYNCED = "synced"
        const val ACTION = "action"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.QUEST_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT_ID} int NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255) NOT NULL,
            ${Columns.ELEMENT} text NOT NULL,
            ${Columns.GEOMETRY} text NOT NULL,
            ${Columns.SOURCE} varchar(255) NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.CREATED_TIMESTAMP} int NOT NULL,
            ${Columns.IS_SYNCED} int NOT NULL,
            ${Columns.ACTION} text
        );
    """

    const val ELEMENT_INDEX_CREATE = """
        CREATE INDEX osm_element_edits_index ON $NAME (
            ${Columns.ELEMENT_TYPE},
            ${Columns.ELEMENT_ID}
        );
    """
}
