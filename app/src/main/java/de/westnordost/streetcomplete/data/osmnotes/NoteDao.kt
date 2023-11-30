package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CLOSED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.COMMENTS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.CREATED
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.ID
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.Columns.STATUS
import de.westnordost.streetcomplete.data.osmnotes.NoteTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Stores OSM notes */
class NoteDao(private val db: Database) {
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
                it.timestampCreated,
                it.timestampClosed,
                Json.encodeToString(it.comments),
                nowAsEpochMilliseconds()
            ) }
        )
    }

    fun getAll(bbox: BoundingBox): List<Note> =
        db.query(NAME, where = inBoundsSql(bbox)) { it.toNote() }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> =
        db.query(NAME,
            columns = arrayOf(LATITUDE, LONGITUDE),
            where = inBoundsSql(bbox),
        ) { LatLon(it.getDouble(LATITUDE), it.getDouble(LONGITUDE)) }

    fun getAll(ids: Collection<Long>): List<Note> {
        if (ids.isEmpty()) return emptyList()
        return db.query(NAME, where = "$ID IN (${ids.joinToString(",")})") { it.toNote() }
    }

    fun getIdsOlderThan(timestamp: Long, limit: Int? = null): List<Long> {
        if (limit != null && limit <= 0) return emptyList()
        else return db.query(NAME,
            columns = arrayOf(ID),
            where = "$LAST_SYNC < $timestamp",
            limit = limit?.toString()
        ) { it.getLong(ID) }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$ID IN (${ids.joinToString(",")})")
    }

    fun clear() {
        db.delete(NAME)
    }

    private fun Note.toPairs() = listOf(
        ID to id,
        LATITUDE to position.latitude,
        LONGITUDE to position.longitude,
        STATUS to status.name,
        CREATED to timestampCreated,
        CLOSED to timestampClosed,
        COMMENTS to Json.encodeToString(comments),
        LAST_SYNC to nowAsEpochMilliseconds()
    )

    private fun CursorPosition.toNote() = Note(
        LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
        getLong(ID),
        getLong(CREATED),
        getLongOrNull(CLOSED),
        Note.Status.valueOf(getString(STATUS)),
        Json.decodeFromString(getString(COMMENTS))
    )

    private fun inBoundsSql(bbox: BoundingBox): String = """
        ($LATITUDE BETWEEN ${bbox.min.latitude} AND ${bbox.max.latitude}) AND
        ($LONGITUDE BETWEEN ${bbox.min.longitude} AND ${bbox.max.longitude})
    """.trimIndent()
}
