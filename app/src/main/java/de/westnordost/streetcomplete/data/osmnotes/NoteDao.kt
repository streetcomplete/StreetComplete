package de.westnordost.streetcomplete.data.osmnotes


import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CLOSED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.COMMENTS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CREATED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.STATUS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.NAME
import de.westnordost.streetcomplete.ktx.*

class NoteDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer
) {

	private val db get() = dbHelper.writableDatabase

    fun putAll(notes: Collection<Note>) {
        db.transaction {
	        for (note in notes) {
		        put(note)
	        }
        }
    }

    fun put(note: Note) {
	    db.insertWithOnConflict(NAME, null, note.createContentValues(), CONFLICT_REPLACE)
    }

    fun get(id: Long): Note? {
        return db.queryOne(NAME, null, "$ID = $id") { it.createNote(serializer) }
    }

    fun delete(id: Long): Boolean {
        return db.delete(NAME, "$ID = $id", null) == 1
    }

    fun deleteUnreferenced(): Int {
        val where = ID + " NOT IN ( " +
                "SELECT " + OsmNoteQuestTable.Columns.NOTE_ID + " FROM " + OsmNoteQuestTable.NAME +
                ")"

        return db.delete(NAME, where, null)
    }

	private fun Note.createContentValues() = contentValuesOf(
		ID to id,
		LATITUDE to position.latitude,
		LONGITUDE to position.longitude,
		STATUS to status.name,
		CREATED to dateCreated.time,
		CLOSED to dateClosed?.time,
		COMMENTS to serializer.toBytes(ArrayList(comments))
	)
}

internal fun Cursor.createNote(serializer: Serializer) = Note().also { n ->
	n.id = getLong(ID)
	n.position = OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
	n.dateCreated = Date(getLong(CREATED))
	n.dateClosed = getLongOrNull(CLOSED)?.let { Date(it) }
	n.status = Note.Status.valueOf(getString(STATUS))
	n.comments = serializer.toObject<ArrayList<NoteComment>>(getBlob(COMMENTS))
}
