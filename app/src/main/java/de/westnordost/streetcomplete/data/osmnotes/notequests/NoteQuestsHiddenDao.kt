package de.westnordost.streetcomplete.data.osmnotes.notequests

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.Columns.NOTE_ID
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable.NAME
import de.westnordost.streetcomplete.ktx.queryOne
import de.westnordost.streetcomplete.ktx.query
import javax.inject.Inject

/** Persists which note ids should be hidden (because the user selected so) in the note quest */
class NoteQuestsHiddenDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {
    private val db get() = dbHelper.writableDatabase

    fun add(noteId: Long) {
        db.insert(NAME, null, contentValuesOf(NOTE_ID to noteId))
    }

    fun contains(noteId: Long): Boolean =
        db.queryOne(NAME, selection = "$NOTE_ID = $noteId") { true } ?: false

    fun getAll(): List<Long> =
        db.query(NAME) { it.getLong(0) }

    fun deleteAll(): Int =
        db.delete(NAME, null, null)
}
