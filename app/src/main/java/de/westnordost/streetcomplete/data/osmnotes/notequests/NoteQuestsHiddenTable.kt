package de.westnordost.streetcomplete.data.osmnotes.notequests

object NoteQuestsHiddenTable {
    const val NAME = "osm_notes_hidden"

    object Columns {
        const val NOTE_ID = "note_id"
        const val TIMESTAMP = "timestamp"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.NOTE_ID} INTEGER PRIMARY KEY,
            ${Columns.TIMESTAMP} int NOT NULL
        );
    """
}
