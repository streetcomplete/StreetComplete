package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.atp.AtpTable.NAME
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.ID
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.OSM_ELEMENT_MATCH_ID
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.OSM_ELEMENT_MATCH_TYPE
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.ATP_TAGS
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.OSM_TAGS
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.atp.AtpTable.Columns.REPORT_TYPE
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.json.Json

/** Stores ATP entries */
class AtpDao(private val db: Database) {
    fun put(entry: AtpEntry) {
        db.replace(NAME, entry.toPairs())
    }

    fun get(id: Long): AtpEntry? =
        db.queryOne(NAME, where = "$ID = $id") { it.toAtpEntry() }

    fun delete(id: Long): Boolean =
        db.delete(NAME, "$ID = $id") == 1

    fun putAll(entries: Collection<AtpEntry>) {
        if (entries.isEmpty()) return

        db.replaceMany(NAME,
            arrayOf(ID, LATITUDE, LONGITUDE, OSM_ELEMENT_MATCH_ID, OSM_ELEMENT_MATCH_TYPE, ATP_TAGS, OSM_TAGS, REPORT_TYPE, LAST_SYNC),
            entries.map { arrayOf(
                it.id,
                it.position.latitude,
                it.position.longitude,
                it.osmMatch?.id,
                it.osmMatch?.type.toString(),
                Json.encodeToString(it.tagsInATP),
                it.tagsInOSM?.let { Json.encodeToString(it) },
                it.reportType.name,
                nowAsEpochMilliseconds()
            ) }
        )
    }

    fun getAll(bbox: BoundingBox): List<AtpEntry> =
        db.query(NAME, where = inBoundsSql(bbox)) { it.toAtpEntry() }

    fun getAllPositions(bbox: BoundingBox): List<LatLon> =
        db.query(NAME,
            columns = arrayOf(LATITUDE, LONGITUDE),
            where = inBoundsSql(bbox),
        ) { LatLon(it.getDouble(LATITUDE), it.getDouble(LONGITUDE)) }

    fun getAll(ids: Collection<Long>): List<AtpEntry> {
        if (ids.isEmpty()) return emptyList()
        return db.query(NAME, where = "$ID IN (${ids.joinToString(",")})") { it.toAtpEntry() }
    }

    fun getAllWithMatchingOsmElement(match: ElementKey): List<AtpEntry> {
        return db.query(NAME,
            where = "$OSM_ELEMENT_MATCH_ID = ? AND $OSM_ELEMENT_MATCH_TYPE = ?",
            args = arrayOf(
                match.id,
                match.type.toString(),
            )
        ) { it.toAtpEntry() }
    }

    fun getIdsOlderThan(timestamp: Long, limit: Int? = null): List<Long> =
        if (limit != null && limit <= 0) {
            emptyList()
        } else {
            db.query(NAME,
                columns = arrayOf(ID),
                where = "$LAST_SYNC < $timestamp",
                limit = limit
            ) { it.getLong(ID) }
        }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        return db.delete(NAME, "$ID IN (${ids.joinToString(",")})")
    }

    fun clear() {
        db.delete(NAME)
    }

    private fun AtpEntry.toPairs() = listOf(
        ID to id,
        LATITUDE to position.latitude,
        LONGITUDE to position.longitude,
        OSM_ELEMENT_MATCH_ID to osmMatch?.id,
        OSM_ELEMENT_MATCH_TYPE to osmMatch?.type?.name?.lowercase(),
        ATP_TAGS to Json.encodeToString(tagsInATP),
        OSM_TAGS to tagsInOSM?.let { Json.encodeToString(it) }, // TODO include tests for null and not null
        LAST_SYNC to nowAsEpochMilliseconds(),
        REPORT_TYPE to reportType.name.lowercase(),
    )

    private fun CursorPosition.toAtpEntry(): AtpEntry {
        val tagsInOsm = getStringOrNull(OSM_TAGS)?.let {
            Json.decodeFromString<Map<String, String>>(it)
        }
        val osmMatchId = getLongOrNull(OSM_ELEMENT_MATCH_ID)
        val osmMatchType = getStringOrNull(OSM_ELEMENT_MATCH_TYPE)
        val osmMatch = if(osmMatchId == null || osmMatchType == null) {
            null
        } else {
            ElementKey( ElementType.valueOf(osmMatchType.uppercase()), osmMatchId)
        }
        val reportType = ReportType.valueOf(getString(REPORT_TYPE).uppercase())

        val atpEntry = AtpEntry(
            LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
            getLong(ID),
            osmMatch,
            Json.decodeFromString(getString(ATP_TAGS)),
            tagsInOsm,
            reportType,
        )
        return atpEntry
    }

    private fun inBoundsSql(bbox: BoundingBox): String = """
        ($LATITUDE BETWEEN ${bbox.min.latitude} AND ${bbox.max.latitude}) AND
        ($LONGITUDE BETWEEN ${bbox.min.longitude} AND ${bbox.max.longitude})
    """.trimIndent()
}
