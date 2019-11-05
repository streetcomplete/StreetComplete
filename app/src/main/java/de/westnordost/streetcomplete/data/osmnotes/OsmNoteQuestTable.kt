package de.westnordost.streetcomplete.data.osmnotes

object OsmNoteQuestTable {
    const val NAME = "osm_notequests"

    const val NAME_MERGED_VIEW = "osm_notequests_full"

    object Columns {
        const val QUEST_ID = "quest_id"
        const val NOTE_ID = NoteTable.Columns.ID
        const val QUEST_STATUS = "quest_status"
        const val COMMENT = "changes"
        const val LAST_UPDATE = "last_update"
        const val IMAGE_PATHS = "image_paths"
    }

    const val CREATE = """
        CREATE TABLE $NAME (
            ${Columns.QUEST_ID} INTEGER PRIMARY KEY,
            ${Columns.QUEST_STATUS} varchar(255) NOT NULL,
            ${Columns.COMMENT} text,
            ${Columns.LAST_UPDATE} int NOT NULL,
            ${Columns.NOTE_ID} INTEGER UNIQUE NOT NULL,
            ${Columns.IMAGE_PATHS} blob
            REFERENCES ${NoteTable.NAME}(${NoteTable.Columns.ID})
        );"""

    const val CREATE_VIEW = """
        CREATE VIEW $NAME_MERGED_VIEW AS
        SELECT * FROM $NAME
            INNER JOIN ${NoteTable.NAME}
            USING (${NoteTable.Columns.ID});"""
}
