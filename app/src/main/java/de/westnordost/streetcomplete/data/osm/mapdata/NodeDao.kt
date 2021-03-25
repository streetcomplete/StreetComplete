package de.westnordost.streetcomplete.data.osm.mapdata

import javax.inject.Inject

import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.NAME
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.lang.System.currentTimeMillis
import java.util.Date

/** Stores OSM nodes */
class NodeDao @Inject constructor(
    private val db: Database,
    private val serializer: Serializer
) {
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
                    node.tags?.let { serializer.toBytes(HashMap<String,String>(it)) },
                    node.dateEdited.time,
                    time
                )
            }
        )
    }

    fun getAll(ids: Collection<Long>): List<Node> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")
        return db.query(NAME, where = "$ID IN ($idsString)") { cursor ->
            OsmNode(
                cursor.getLong(ID),
                cursor.getInt(VERSION),
                OsmLatLon(cursor.getDouble(LATITUDE), cursor.getDouble(LONGITUDE)),
                cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) },
                null,
                Date(cursor.getLong(TIMESTAMP))
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
