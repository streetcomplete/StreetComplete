package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Stores OSM nodes */
class NodeDao(private val db: Database) {
    fun put(node: Node) {
        putAll(listOf(node))
    }

    fun get(id: Long): Node? =
        getAll(listOf(id)).firstOrNull()

    fun delete(id: Long): Boolean =
        deleteAll(listOf(id)) > 0

    fun putAll(nodes: Collection<Node>) {
        if (nodes.isEmpty()) return

        val time = nowAsEpochMilliseconds()

        db.replaceMany(NAME,
            arrayOf(ID, VERSION, LATITUDE, LONGITUDE, TAGS, TIMESTAMP, LAST_SYNC),
            nodes.map { node ->
                arrayOf(
                    node.id,
                    node.version,
                    node.position.latitude,
                    node.position.longitude,
                    if (node.tags.isNotEmpty()) Json.encodeToString(node.tags) else null,
                    node.timestampEdited,
                    time
                )
            }
        )
    }

    fun getAll(ids: Collection<Long>): List<Node> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")
        return db.query(NAME, where = "$ID IN ($idsString)") { it.toNode() }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.delete(NAME, "$ID IN ($idsString)")
    }

    fun clear() {
        db.delete(NAME)
    }

    fun getIdsOlderThan(timestamp: Long, limit: Int? = null): List<Long> {
        if (limit != null && limit <= 0) return emptyList()
        return db.query(NAME,
            columns = arrayOf(ID),
            where = "$LAST_SYNC < $timestamp",
            limit = limit?.toString()
        ) { it.getLong(ID) }
    }

    fun getAllIds(bbox: BoundingBox): List<Long> =
        db.query(NAME, where = inBoundsSql(bbox), columns = arrayOf(ID)) { it.getLong(ID) }

    fun getAllAsGeometryEntries(ids: Collection<Long>): List<ElementGeometryEntry> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")
        return db.query(NAME,
            where = "$ID IN ($idsString)",
            columns = arrayOf(ID, LATITUDE, LONGITUDE)
        ) { it.toElementGeometryEntry() }
    }

    fun getAll(bbox: BoundingBox): List<Node> =
        db.query(NAME, where = inBoundsSql(bbox)) { it.toNode() }
}

private fun CursorPosition.toNode() = Node(
    getLong(ID),
    LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)),
    getStringOrNull(TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap(),
    getInt(VERSION),
    getLong(TIMESTAMP),
)

private fun CursorPosition.toElementGeometryEntry() = ElementGeometryEntry(
    ElementType.NODE,
    getLong(ID),
    ElementPointGeometry(LatLon(getDouble(LATITUDE), getDouble(LONGITUDE)))
)

private fun inBoundsSql(bbox: BoundingBox): String = """
    ($LATITUDE BETWEEN ${bbox.min.latitude} AND ${bbox.max.latitude}) AND
    ($LONGITUDE BETWEEN ${bbox.min.longitude} AND ${bbox.max.longitude})
""".trimIndent()
