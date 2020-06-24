package de.westnordost.streetcomplete.data.osmnotes.createnotes

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf

import javax.inject.Inject

import de.westnordost.streetcomplete.data.WhereSelectionBuilder
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.ELEMENT_ID
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.ELEMENT_TYPE
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.IMAGE_PATHS
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.QUEST_TITLE
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.Columns.TEXT
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable.NAME
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Singleton

/** Stores CreateNote objects - for creating OSM notes */
@Singleton class CreateNoteDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val mapping: CreateNoteMapping
) {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val db get() = dbHelper.writableDatabase

    interface Listener {
        fun onAddedCreateNote()
        fun onDeletedCreateNote()
    }

    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()

    fun add(note: CreateNote): Boolean {
        val rowId = db.insert(NAME, null, mapping.toContentValues(note))
        if (rowId == -1L) return false

        note.id = rowId
        listeners.forEach { it.onAddedCreateNote() }

        return true
    }

    fun get(id: Long): CreateNote? {
        return db.queryOne(NAME, null, "$ID = $id") { mapping.toObject(it) }
    }

    fun getCount(): Int {
        return db.queryOne(NAME, arrayOf("COUNT(*)")) { it.getInt(0) } ?: 0
    }

    fun delete(id: Long): Boolean {
        val success = db.delete(NAME, "$ID = $id", null) == 1
        if (!success) return false
        listeners.forEach { it.onDeletedCreateNote() }
        return true
    }

    fun getAll(): List<CreateNote> {
        return db.query(NAME) { mapping.toObject(it) }
    }

    fun getAll(bbox: BoundingBox): List<CreateNote> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bbox)

        return db.query(NAME, null, builder.where, builder.args) { mapping.toObject(it) }
    }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> {
        val builder = WhereSelectionBuilder()
        builder.appendBounds(bbox)
        val cols = arrayOf(LATITUDE, LONGITUDE)
        return db.query(NAME, cols, builder.where, builder.args) { OsmLatLon(it.getDouble(0), it.getDouble(1)) }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
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

class CreateNoteMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<CreateNote> {

    override fun toContentValues(obj: CreateNote) = contentValuesOf(
        LATITUDE to obj.position.latitude,
        LONGITUDE to obj.position.longitude,
        ELEMENT_TYPE to obj.elementKey?.elementType?.name,
        ELEMENT_ID to obj.elementKey?.elementId,
        IMAGE_PATHS to obj.imagePaths?.let { serializer.toBytes(ArrayList(it)) },
        TEXT to obj.text,
        QUEST_TITLE to obj.questTitle
    )

    override fun toObject(cursor: Cursor) = CreateNote(
            cursor.getLong(ID),
            cursor.getString(TEXT),
            OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
            cursor.getStringOrNull(QUEST_TITLE),
            cursor.getStringOrNull(ELEMENT_TYPE)?.let { type ->
                cursor.getLongOrNull(ELEMENT_ID)?.let { id ->
                    ElementKey(Element.Type.valueOf(type), id)
                }
            },
            cursor.getBlobOrNull(IMAGE_PATHS)?.let { serializer.toObject<ArrayList<String>>(it) }
    )
}
