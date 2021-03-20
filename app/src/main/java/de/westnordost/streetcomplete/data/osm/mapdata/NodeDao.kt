package de.westnordost.streetcomplete.data.osm.mapdata

import javax.inject.Inject

import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LATITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.LONGITUDE
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable.NAME
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.lang.System.currentTimeMillis

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
            arrayOf(ID, VERSION, LATITUDE, LONGITUDE, TAGS, LAST_UPDATE),
            nodes.map { node ->
                arrayOf(
                    node.id,
                    node.version,
                    node.position.latitude,
                    node.position.longitude,
                    node.tags?.let { serializer.toBytes(HashMap<String,String>(it)) },
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
                cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
            )
        }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.delete(NAME, "$ID IN ($idsString)")
    }

    fun getIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, columns = arrayOf(ID), where = "$LAST_UPDATE < $timestamp") { it.getLong(ID) }
}
