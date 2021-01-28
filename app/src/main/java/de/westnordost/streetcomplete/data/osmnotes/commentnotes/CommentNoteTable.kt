package de.westnordost.streetcomplete.data.osmnotes.commentnotes

object CommentNoteTable {
    const val NAME = "osm_comment_notes"

    object Columns {
        const val NOTE_ID = "note_id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val TEXT = "text"
        const val IMAGE_PATHS = "image_paths"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.NOTE_ID} INTEGER PRIMARY KEY,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.TEXT} text NOT NULL,
            ${Columns.IMAGE_PATHS} blob
        );"""
}
