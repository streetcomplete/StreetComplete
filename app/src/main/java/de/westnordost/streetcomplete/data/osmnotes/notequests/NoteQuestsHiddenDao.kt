package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.Columns.NOTE_ID
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.NAME
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/** Persists which note ids should be hidden (because the user selected so) in the note quest */
class NoteQuestsHiddenDao @Inject constructor(private val db: Database) {

    fun add(noteId: Long) {
        db.insert(NAME, listOf(
            NOTE_ID to noteId,
            TIMESTAMP to currentTimeMillis()
        ))
    }

    fun contains(noteId: Long): Boolean =
        db.queryOne(NAME, where = "$NOTE_ID = $noteId") { true } ?: false

    fun delete(noteId: Long): Boolean =
        db.delete(NAME, where = "$NOTE_ID = $noteId") == 1

    fun getNewerThan(timestamp: Long): List<HiddenNoteQuest> =
        db.query(NAME, where = "$TIMESTAMP > $timestamp") { it.toHiddenNoteQuest() }

    fun getAllIds(): List<Long> =
        db.query(NAME) { it.getLong(NOTE_ID) }

    fun deleteAll(): Int =
        db.delete(NAME)
}

private fun CursorPosition.toHiddenNoteQuest() =
    HiddenNoteQuest(getLong(NOTE_ID), getLong(TIMESTAMP))

data class HiddenNoteQuest(val noteId: Long, val createdTimestamp: Long)
