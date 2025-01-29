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

    fun getTimestamp(noteId: Long): Long? =
        db.queryOne(NAME, where = "$NOTE_ID = $noteId") { it.getLong(TIMESTAMP) }

    fun delete(noteId: Long): Boolean =
        db.delete(NAME, where = "$NOTE_ID = $noteId") == 1

    fun getNewerThan(timestamp: Long): List<NoteQuestHiddenAt> =
        db.query(NAME, where = "$TIMESTAMP > $timestamp") { it.toNoteQuestHiddenAt() }

    fun getAll(): List<NoteQuestHiddenAt> =
        db.query(NAME) { it.toNoteQuestHiddenAt() }

    fun deleteAll(): Int =
        db.delete(NAME)

    fun countAll(): Int =
        db.queryOne(NAME, columns = arrayOf("COUNT(*)")) { it.getInt("COUNT(*)") } ?: 0
}

private fun CursorPosition.toNoteQuestHiddenAt() =
    NoteQuestHiddenAt(getLong(NOTE_ID), getLong(TIMESTAMP))

data class NoteQuestHiddenAt(val noteId: Long, val timestamp: Long)
