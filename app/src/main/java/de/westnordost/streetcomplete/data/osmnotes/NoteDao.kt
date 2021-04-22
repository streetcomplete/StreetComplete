package de.westnordost.streetcomplete.data.osmnotes


import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon

import java.util.ArrayList
import java.util.Date

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NoteComment
import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CLOSED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.COMMENTS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CREATED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.STATUS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.NAME
import de.westnordost.streetcomplete.ktx.*
import java.lang.System.currentTimeMillis

/** Stores OSM notes */
class NoteDao @Inject constructor(
    private val db: Database,
    private val serializer: Serializer
) {
    fun put(note: Note) {
        db.replace(NAME, note.toPairs())
    }

    fun get(id: Long): Note? =
        db.queryOne(NAME, where = "$ID = $id") { it.toNote() }

    fun delete(id: Long): Boolean =
        db.delete(NAME, "$ID = $id") == 1

    fun putAll(notes: Collection<Note>) {
        if (notes.isEmpty()) return

        db.replaceMany(NAME,
            arrayOf(ID, LATITUDE, LONGITUDE, STATUS, CREATED, CLOSED, COMMENTS, LAST_SYNC),
            notes.map { arrayOf(
                it.id,
                it.position.latitude,
                it.position.longitude,
                it.status.name,
                it.dateCreated.time,
                it.dateClosed?.time,
                serializer.toBytes(ArrayList(it.comments)),
                currentTimeMillis()
            ) }
        )
    }

    fun getAll(bbox: BoundingBox): List<Note> =
        db.query(NAME, where = inBoundsSql(bbox)) { it.toNote() }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> =
        db.query(NAME,
            columns = arrayOf(LATITUDE, LONGITUDE),
            where = inBoundsSql(bbox),
        ) { OsmLatLon(it.getDouble(LATITUDE), it.getDouble(LONGITUDE)) }

    fun getAll(ids: Collection<Long>): List<Note> {
        if (ids.isEmpty()) return emptyList()
        return db.query(NAME, where = "$ID IN (${ids.joinToString(",")})") { it.toNote() }
    }

    fun getAllIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, arrayOf(ID), "$LAST_SYNC < $timestamp") { it.getLong(ID) }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$ID IN (${ids.joinToString(",")})")
    }

    private fun Note.toPairs() = listOf(
        ID to id,
        LATITUDE to position.latitude,
        LONGITUDE to position.longitude,
        STATUS to status.name,
        CREATED to dateCreated.time,
        CLOSED to dateClosed?.time,
        COMMENTS to serializer.toBytes(ArrayList(comments)),
        LAST_SYNC to currentTimeMillis()
    )

    private fun CursorPosition.toNote() = Note().also { n ->
        n.id = getLong(ID)
        n.position = OsmLatLon(getDouble(LATITUDE), getDouble(LONGITUDE))
        n.dateCreated = Date(getLong(CREATED))
        n.dateClosed = getLongOrNull(CLOSED)?.let { Date(it) }
        n.status = Note.Status.valueOf(getString(STATUS))
        n.comments = serializer.toObject<ArrayList<NoteComment>>(getBlob(COMMENTS))
    }

    private fun inBoundsSql(bbox: BoundingBox): String = """
        ($LATITUDE BETWEEN ${bbox.minLatitude} AND ${bbox.maxLatitude}) AND
        ($LONGITUDE BETWEEN ${bbox.minLongitude} AND ${bbox.maxLongitude})
    """.trimIndent()

}
