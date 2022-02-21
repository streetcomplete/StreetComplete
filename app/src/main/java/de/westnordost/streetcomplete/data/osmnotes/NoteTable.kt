package de.westnordost.streetcomplete.data.osmnotes

object NoteTable {
    const val NAME = "osm_notes"

    object Columns {
        const val ID = "note_id"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val STATUS = "note_status"
        const val CREATED = "note_created"
        const val CLOSED = "note_closed"
        const val COMMENTS = "comments"
        const val LAST_SYNC = "last_sync"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.ID} int PRIMARY KEY,
            ${Columns.LATITUDE} double NOT NULL,
            ${Columns.LONGITUDE} double NOT NULL,
            ${Columns.CREATED} int NOT NULL,
            ${Columns.CLOSED} int,
            ${Columns.STATUS} varchar(255) NOT NULL,
            ${Columns.COMMENTS} text NOT NULL,
            ${Columns.LAST_SYNC} int NOT NULL
        );
    """

    const val SPATIAL_INDEX_CREATE = """
        CREATE INDEX osm_notes_spatial_index ON $NAME (
            ${Columns.LATITUDE},
            ${Columns.LONGITUDE}
        );
    """
}
