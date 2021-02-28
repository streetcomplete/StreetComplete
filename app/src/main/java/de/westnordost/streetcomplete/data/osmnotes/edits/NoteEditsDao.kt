package de.westnordost.streetcomplete.data.osmnotes.edits

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
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
    private val mapping: NoteEditsMapping
){
    private val db get() = dbHelper.writableDatabase

    fun add(edit: NoteEdit): Boolean {
        db.transaction {
            val rowId = db.insert(NAME, null, mapping.toContentValues(edit))
            if (rowId == -1L) return false
            // if the note id is not set, set it to the negative of the row id
            if (edit.noteId < 0) {
                db.update(NAME, contentValuesOf(NOTE_ID to -rowId), "$ID = $rowId", null)
            }
        }
        return true
    }

    fun get(id: Long): NoteEdit? =
        db.queryOne(NAME, selection = "$ID = $id") { mapping.toObject(it) }

    fun getAll(): List<NoteEdit> =
        db.query(NAME, orderBy = "$IS_SYNCED, $CREATED_TIMESTAMP") { mapping.toObject(it) }

    fun getOldestUnsynced(): NoteEdit? =
        db.queryOne(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getUnsyncedCount(): Int =
        db.queryOne(NAME, arrayOf("COUNT(*)"), "$IS_SYNCED = 0") { it.getInt(0) } ?: 0

    fun getAllUnsynced(): List<NoteEdit> =
        db.query(NAME, selection = "$IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getAllUnsyncedForNote(noteId: Long): List<NoteEdit> =
        db.query(NAME, selection = "$NOTE_ID = $noteId AND $IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

    fun getAllUnsyncedForNotes(noteIds: Collection<Long>): List<NoteEdit> {
        val notes = noteIds.joinToString(",")
        return db.query(NAME, selection = "$NOTE_ID IN ($notes) AND $IS_SYNCED = 0", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }
    }

    fun getAllUnsynced(bbox: BoundingBox): List<NoteEdit> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bbox)
        builder.add("$IS_SYNCED = 0")
        return db.query(NAME,
            selection = builder.where,
            selectionArgs = builder.args,
            orderBy = CREATED_TIMESTAMP
        ) { mapping.toObject(it) }
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
        db.queryOne(NAME, selection = "$IS_SYNCED = 1 AND $IMAGES_NEED_ACTIVATION = 1", orderBy = CREATED_TIMESTAMP) { mapping.toObject(it) }

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
}

class NoteEditsMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<NoteEdit> {

    override fun toContentValues(obj: NoteEdit) = contentValuesOf(
        NOTE_ID to obj.noteId,
        LATITUDE to obj.position.latitude,
        LONGITUDE to obj.position.longitude,
        CREATED_TIMESTAMP to obj.createdTimestamp,
        IS_SYNCED to if (obj.isSynced) 1 else 0,
        TEXT to obj.text,
        IMAGE_PATHS to serializer.toBytes(ArrayList<String>(obj.imagePaths)),
        IMAGES_NEED_ACTIVATION to if (obj.imagesNeedActivation) 1 else 0,
        TYPE to obj.action.name
    )

    override fun toObject(cursor: Cursor) = NoteEdit(
        cursor.getLong(ID),
        cursor.getLong(NOTE_ID),
        OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
        cursor.getLong(CREATED_TIMESTAMP),
        cursor.getInt(IS_SYNCED) == 1,
        cursor.getStringOrNull(TEXT),
        serializer.toObject<ArrayList<String>>(cursor.getBlob(IMAGE_PATHS)),
        cursor.getInt(IMAGES_NEED_ACTIVATION) == 1,
        NoteEditAction.valueOf(cursor.getString(TYPE))
    )
}
