package de.westnordost.streetcomplete.data.osmnotes

object CreateNoteTable {
    const val NAME = "osm_create_notes"

    object Columns {
        const val ID = "create_id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val TEXT = "text"
        const val ELEMENT_TYPE = "element_type"
        const val ELEMENT_ID = "element_id"
        const val QUEST_TITLE = "quest_title"
        const val IMAGE_PATHS = "image_paths"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} INTEGER PRIMARY KEY,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.ELEMENT_TYPE} varchar(255),
            ${Columns.ELEMENT_ID} int,
            ${Columns.TEXT} text NOT NULL,
            ${Columns.QUEST_TITLE} text,
            ${Columns.IMAGE_PATHS} blob
        );"""
}
