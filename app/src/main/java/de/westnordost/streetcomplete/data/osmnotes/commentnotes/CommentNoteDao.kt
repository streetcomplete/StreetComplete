package de.westnordost.streetcomplete.data.osmnotes.commentnotes

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon

import javax.inject.Inject
import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable.Columns.IMAGE_PATHS
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable.Columns.NOTE_ID
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable.Columns.TEXT
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable.NAME

import de.westnordost.streetcomplete.ktx.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores CommentNote objects - for commenting on OSM notes */
@Singleton class CommentNoteDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: CommentNoteMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     * database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedCommentNote(note: CommentNote)
        fun onDeletedCommentNote(noteId: Long)
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun add(note: CommentNote): Boolean {
        val rowId = db.insert(NAME, null, mapping.toContentValues(note))
        if (rowId == -1L) return false
        listeners.forEach { it.onAddedCommentNote(note) }
        return true
    }

    fun get(id: Long): CommentNote? {
        return db.queryOne(NAME, null, "$NOTE_ID = $id") { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun delete(id: Long): Boolean {
        val success = db.delete(NAME, "$NOTE_ID = $id", null) == 1
        if (!success) return false
        listeners.forEach { it.onDeletedCommentNote(id) }
        return true
    }

    fun getAll(): List<CommentNote> {
        return db.query(NAME) { mapping.toObject(it) }
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> {
        return db.query(NAME,
            arrayOf(LATITUDE, LONGITUDE),
            "($LATITUDE BETWEEN ? AND ?) AND ($LONGITUDE BETWEEN ? AND ?)",
            arrayOf(
                bbox.minLatitude.toString(),
                bbox.maxLatitude.toString(),
                bbox.minLongitude.toString(),
                bbox.maxLongitude.toString()
            )
        ) { OsmLatLon(it.getDouble(0), it.getDouble(1)) }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}

class CommentNoteMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<CommentNote> {

    override fun toContentValues(obj: CommentNote) = contentValuesOf(
        NOTE_ID to obj.noteId,
        LATITUDE to obj.position.latitude,
        LONGITUDE to obj.position.longitude,
        TEXT to obj.text,
        IMAGE_PATHS to obj.imagePaths?.let { serializer.toBytes(ArrayList(it)) }
    )

    override fun toObject(cursor: Cursor) = CommentNote(
        cursor.getLong(NOTE_ID),
        OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
        cursor.getString(TEXT),
        cursor.getBlobOrNull(IMAGE_PATHS)?.let { serializer.toObject<ArrayList<String>>(it) }
    )
}
