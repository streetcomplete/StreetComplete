package de.westnordost.streetcomplete.data.osmnotes.edits

object NoteEditsTable {
    const val NAME = "osm_note_edits"

    object Columns {
        const val ID = "id"
        const val NOTE_ID = "note_id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val CREATED_TIMESTAMP = "created"
        const val IS_SYNCED = "synced"
        const val TYPE = "type"
        const val TEXT = "text"
        const val IMAGE_PATHS = "image_paths"
        const val IMAGES_NEED_ACTIVATION = "images_need_activation"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
            ${Columns.NOTE_ID} int NOT NULL,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.CREATED_TIMESTAMP} int NOT NULL,
            ${Columns.IS_SYNCED} int NOT NULL,
            ${Columns.TEXT} text,
            ${Columns.IMAGE_PATHS} blob NOT NULL,
            ${Columns.IMAGES_NEED_ACTIVATION} int NOT NULL,
            ${Columns.TYPE} varchar(255)
        );"""
}
