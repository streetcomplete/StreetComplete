package de.westnordost.streetcomplete.data.osmnotes.edits

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.WhereSelectionBuilder
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.CREATED_TIMESTAMP
import de.westnordost.streetcomplete.util.Serializer
import javax.inject.Inject
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.IMAGES_NEED_ACTIVATION
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.IMAGE_PATHS
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.IS_SYNCED
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.NOTE_ID
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.TEXT
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.Columns.TYPE
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable.NAME
import de.westnordost.streetcomplete.ktx.*

class NoteEditsDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer
){
    private val db get() = dbHelper.writableDatabase

    fun add(edit: NoteEdit): Boolean {
        db.transaction {
            val rowId = db.insert(NAME, null, edit.toContentValues())
            if (rowId == -1L) return false
            edit.id = rowId
            // if the note id is not set, set it to the negative of the row id
            if (edit.noteId == 0L) {
                db.update(NAME, contentValuesOf(NOTE_ID to -rowId), "$ID = $rowId", null)
                edit.noteId = -rowId
            }
        }
        return true
    }

    fun get(id: Long): NoteEdit? =
        db.queryOne(NAME, selection = "$ID = $id") { it.toNoteEdit() }

    fun getAll(): List<NoteEdit> =
        db.query(NAME, orderBy = "$IS_SYNCED, $CREATED_TIMESTAMP") { it.toNoteEdit() }

    fun getOldestUnsynced(): NoteEdit? =
        db.queryOne(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { it.toNoteEdit() }

    fun getUnsyncedCount(): Int =
        db.queryOne(NAME, arrayOf("COUNT(*)"), "$IS_SYNCED = 0") { it.getInt(0) } ?: 0

    fun getAllUnsynced(): List<NoteEdit> =
        db.query(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { it.toNoteEdit() }

    fun getAllUnsyncedForNote(noteId: Long): List<NoteEdit> =
        db.query(NAME, selection = "$NOTE_ID = $noteId AND $IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { it.toNoteEdit() }

    fun getAllUnsyncedForNotes(noteIds: Collection<Long>): List<NoteEdit> {
        val notes = noteIds.joinToString(",")
        return db.query(NAME, selection = "$NOTE_ID IN ($notes) AND $IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { it.toNoteEdit() }
    }

    fun getAllUnsynced(bbox: BoundingBox): List<NoteEdit> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bbox)
        builder.add("$IS_SYNCED = 0")
        return db.query(NAME,
            selection = builder.where,
            selectionArgs = builder.args,
            orderBy = CREATED_TIMESTAMP
        ) { it.toNoteEdit() }
    }

    fun getAllUnsyncedPositions(bbox: BoundingBox): List<LatLon> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bbox)
        builder.add("$IS_SYNCED = 0")
        return db.query(NAME,
            columns = arrayOf(LATITUDE, LONGITUDE),
            selection = builder.where,
            selectionArgs = builder.args,
            orderBy = CREATED_TIMESTAMP
        ) { OsmLatLon(it.getDouble(0), it.getDouble(1)) }
    }

    fun markSynced(id: Long): Boolean =
        db.update(NAME, contentValuesOf(IS_SYNCED to 1), "$ID = $id", null) == 1

    fun delete(id: Long): Boolean =
        db.delete(NAME, "$ID = $id", null) == 1

    fun deleteSyncedOlderThan(timestamp: Long): Int =
        db.delete(NAME, "$IS_SYNCED = 1 AND $CREATED_TIMESTAMP < $timestamp", null)

    fun updateNoteId(oldNoteId: Long, newNoteId: Long): Int =
        db.update(NAME, contentValuesOf(NOTE_ID to newNoteId), "$NOTE_ID = $oldNoteId", null)

    fun getOldestNeedingImagesActivation(): NoteEdit? =
        db.queryOne(NAME, selection = "$IS_SYNCED = 1 AND $IMAGES_NEED_ACTIVATION = 1", orderBy = CREATED_TIMESTAMP) { it.toNoteEdit() }

    fun markImagesActivated(id: Long): Boolean =
        db.update(NAME, contentValuesOf(IMAGES_NEED_ACTIVATION to 0), "$ID = $id", null) == 1

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

    private fun NoteEdit.toContentValues() = contentValuesOf(
        NOTE_ID to noteId,
        LATITUDE to position.latitude,
        LONGITUDE to position.longitude,
        CREATED_TIMESTAMP to createdTimestamp,
        IS_SYNCED to if (isSynced) 1 else 0,
        TEXT to text,
        IMAGE_PATHS to serializer.toBytes(ArrayList<String>(imagePaths)),
        IMAGES_NEED_ACTIVATION to if (imagesNeedActivation) 1 else 0,
        TYPE to action.name
    )

    private fun Cursor.toNoteEdit() = NoteEdit(
        getLong(ID),
        getLong(NOTE_ID),
        OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
        NoteEditAction.valueOf(getString(TYPE)),
        getStringOrNull(TEXT),
        serializer.toObject<ArrayList<String>>(getBlob(IMAGE_PATHS)),
        getLong(CREATED_TIMESTAMP),
        getInt(IS_SYNCED) == 1,
        getInt(IMAGES_NEED_ACTIVATION) == 1
    )
}
