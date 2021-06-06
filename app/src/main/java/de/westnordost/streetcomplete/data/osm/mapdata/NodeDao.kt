package de.westnordost.streetcomplete.data.osm.mapdata

import javax.inject.Inject

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.NAME
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.System.currentTimeMillis

/** Stores OSM nodes */
class NodeDao @Inject constructor(private val db: Database) {
    fun put(node: Node) {
        putAll(listOf(node))
    }

    fun get(id: Long): Node? =
        getAll(listOf(id)).firstOrNull()

    fun delete(id: Long): Boolean =
        deleteAll(listOf(id)) > 0

    fun putAll(nodes: Collection<Node>) {
        if (nodes.isEmpty()) return

        val time = currentTimeMillis()

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
        return db.query(NAME, where = "$ID IN ($idsString)") { cursor ->
            Node(
                cursor.getLong(ID),
                LatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
                cursor.getStringOrNull(TAGS)?.let { Json.decodeFromString(it) } ?: emptyMap(),
                cursor.getInt(VERSION),
                cursor.getLong(TIMESTAMP)
            )
        }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.delete(NAME, "$ID IN ($idsString)")
    }

    fun getIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, columns = arrayOf(ID), where = "$LAST_SYNC < $timestamp") { it.getLong(ID) }
}
