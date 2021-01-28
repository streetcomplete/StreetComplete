package de.westnordost.streetcomplete.data.osmnotes.notequests

object HiddenNoteQuestTable {
    const val NAME = "osm_hidden_notes"

    object Columns {
        const val NOTE_ID = "note_id"
    }

    const val CREATE = "CREATE TABLE $NAME (${Columns.NOTE_ID} INTEGER PRIMARY KEY);"
}
