package de.westnordost.streetcomplete.data.osmnotes

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.WhereSelectionBuilder
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.osm.ElementKey
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.IMAGE_PATHS
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.QUEST_TITLE
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.Columns.TEXT
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteTable.NAME
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer

class CreateNoteDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer
) {
	private val db get() = dbHelper.writableDatabase

    fun add(note: CreateNote): Boolean {
        val rowId = db.insert(NAME, null, note.createContentValues())
	    if (rowId == -1L) return false

        note.id = rowId
        return true
    }

    fun get(id: Long): CreateNote? {
        val selection =  "$ID = ?"
        val args = arrayOf(id.toString())
	    return db.queryOne(NAME, null, selection, args) { it.createCreateNote() }
    }

	fun getCount(): Int {
		return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
	}

    fun delete(id: Long): Boolean {
	    return db.delete(NAME, "$ID = $id", null) == 1
    }

	fun getAll(): List<CreateNote> {
		return db.query(NAME) { it.createCreateNote() }
	}

    fun getAll(bbox: BoundingBox): List<CreateNote> {
        val builder = WhereSelectionBuilder()
	    builder.appendBounds(bbox)

	    return db.query(NAME, null, builder.where, builder.args) { it.createCreateNote() }
    }

	private fun CreateNote.createContentValues() = contentValuesOf(
		LATITUDE to position.latitude,
		LONGITUDE to position.longitude,
		ELEMENT_TYPE to elementKey?.elementType?.name,
		ELEMENT_ID to elementKey?.elementId,
		IMAGE_PATHS to imagePaths?.let { serializer.toBytes(ArrayList(it)) },
		TEXT to text,
		QUEST_TITLE to questTitle
	)

	private fun Cursor.createCreateNote() = CreateNote(
		getLong(ID),
		getString(TEXT),
		OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
		getStringOrNull(QUEST_TITLE),
		getStringOrNull(ELEMENT_TYPE)?.let { type ->
            getLongOrNull(ELEMENT_ID)?.let { id -> ElementKey(Element.Type.valueOf(type), id) } },
		getBlobOrNull(IMAGE_PATHS)?.let { serializer.toObject<ArrayList<String>>(it) }
	)
}

private fun WhereSelectionBuilder.appendBounds(bbox: BoundingBox) {
	appendAnd("($LATITUDE BETWEEN ? AND ?)",
		bbox.minLatitude.toString(),
		bbox.maxLatitude.toString()
	)
	appendAnd(
		"($LONGITUDE BETWEEN ? AND ?)",
		bbox.minLongitude.toString(),
		bbox.maxLongitude.toString()
	)
}
