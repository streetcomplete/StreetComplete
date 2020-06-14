package de.westnordost.streetcomplete.data.osmnotes


import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.WhereSelectionBuilder
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CLOSED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.COMMENTS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CREATED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.STATUS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.NAME
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestTable
import de.westnordost.streetcomplete.ktx.*

/** Stores OSM notes */
class NoteDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: NoteMapping
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
        db.replaceOrThrow(NAME, null, mapping.toContentValues(note))
    }

    fun get(id: Long): Note? {
        return db.queryOne(NAME, null, "$ID = $id") { mapping.toObject(it) }
    }

    fun delete(id: Long): Boolean {
        return db.delete(NAME, "$ID = $id", null) == 1
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> {
        val cols = arrayOf(LATITUDE, LONGITUDE)
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bbox)
        return db.query(NAME, cols, builder.where, builder.args) { OsmLatLon(it.getDouble(0), it.getDouble(1)) }
    }

    fun deleteUnreferenced(): Int {
        val where = ID + " NOT IN ( " +
                "SELECT " + OsmNoteQuestTable.Columns.NOTE_ID + " FROM " + OsmNoteQuestTable.NAME +
                ")"

        return db.delete(NAME, where, null)
    }
}

private fun WhereSelectionBuilder.appendBounds(bbox: BoundingBox) {
    add("($LATITUDE BETWEEN ? AND ?)",
        bbox.minLatitude.toString(),
        bbox.maxLatitude.toString()
    )
    add(
        "($LONGITUDE BETWEEN ? AND ?)",
        bbox.minLongitude.toString(),
        bbox.maxLongitude.toString()
    )
}

class NoteMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<Note> {

    override fun toContentValues(obj: Note) = contentValuesOf(
        ID to obj.id,
        LATITUDE to obj.position.latitude,
        LONGITUDE to obj.position.longitude,
        STATUS to obj.status.name,
        CREATED to obj.dateCreated.time,
        CLOSED to obj.dateClosed?.time,
        COMMENTS to serializer.toBytes(ArrayList(obj.comments))
    )

    override fun toObject(cursor: Cursor) = Note().also { n ->
        n.id = cursor.getLong(ID)
        n.position = OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE))
        n.dateCreated = Date(cursor.getLong(CREATED))
        n.dateClosed = cursor.getLongOrNull(CLOSED)?.let { Date(it) }
        n.status = Note.Status.valueOf(cursor.getString(STATUS))
        n.comments = serializer.toObject<ArrayList<NoteComment>>(cursor.getBlob(COMMENTS))
    }
}
