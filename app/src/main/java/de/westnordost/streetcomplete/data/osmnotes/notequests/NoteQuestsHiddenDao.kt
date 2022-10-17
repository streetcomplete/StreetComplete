package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.Columns.NOTE_ID
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Persists which note ids should be hidden (because the user selected so) in the note quest */
class NoteQuestsHiddenDao(private val db: Database) {

    fun add(noteId: Long) {
        db.insert(NAME, listOf(
            NOTE_ID to noteId,
            TIMESTAMP to nowAsEpochMilliseconds()
        ))
    }

    fun contains(noteId: Long): Boolean =
        getTimestamp(noteId) != null

    fun getTimestamp(noteId: Long): Long? =
        db.queryOne(NAME, where = "$NOTE_ID = $noteId") { it.getLong(TIMESTAMP) }

    fun delete(noteId: Long): Boolean =
        db.delete(NAME, where = "$NOTE_ID = $noteId") == 1

    fun getNewerThan(timestamp: Long): List<NoteIdWithTimestamp> =
        db.query(NAME, where = "$TIMESTAMP > $timestamp") { it.toNoteIdWithTimestamp() }

    fun getAllIds(): List<Long> =
        db.query(NAME) { it.getLong(NOTE_ID) }

    fun deleteAll(): Int =
        db.delete(NAME)
}

private fun CursorPosition.toNoteIdWithTimestamp() =
    NoteIdWithTimestamp(getLong(NOTE_ID), getLong(TIMESTAMP))

data class NoteIdWithTimestamp(val noteId: Long, val timestamp: Long)
