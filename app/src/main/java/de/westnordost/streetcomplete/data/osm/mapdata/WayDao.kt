package de.westnordost.streetcomplete.data.osm.mapdata

import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.Database

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.INDEX
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.NODE_ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME_NODES
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer
import java.lang.System.currentTimeMillis
import java.util.Date

/** Stores OSM ways */
class WayDao @Inject constructor(
    private val db: Database,
    private val serializer: Serializer
) {
    fun put(way: Way) {
        putAll(listOf(way))
    }

    fun get(id: Long): Way? =
        getAll(listOf(id)).firstOrNull()

    fun delete(id: Long): Boolean =
        deleteAll(listOf(id)) > 0

    fun putAll(ways: Collection<Way>) {
        if (ways.isEmpty()) return
        val idsString = ways.joinToString(",") { it.id.toString() }

        val time = currentTimeMillis()

        db.transaction {
            db.delete(NAME_NODES, "$ID IN ($idsString)")

            db.insertMany(NAME_NODES,
                arrayOf(ID, NODE_ID, INDEX),
                ways.flatMap { way ->
                    way.nodeIds.mapIndexed { index, nodeId ->
                        arrayOf(way.id, nodeId, index)
                    }
                }
            )

            db.replaceMany(NAME,
                arrayOf(ID, VERSION, TAGS, TIMESTAMP, LAST_SYNC),
                ways.map { way ->
                    arrayOf(
                        way.id,
                        way.version,
                        way.tags?.let { serializer.toBytes(HashMap<String,String>(it)) },
                        way.dateEdited.time,
                        time
                    )
                }
            )
        }
    }

    fun getAll(ids: Collection<Long>): List<Way> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")

        val nodeIdsByWayId = mutableMapOf<Long, MutableList<Long>>()
        db.query(NAME_NODES, where = "$ID IN ($idsString)", orderBy = "$ID, $INDEX") { c ->
            val nodeIds = nodeIdsByWayId.getOrPut(c.getLong(ID)) { ArrayList() }
            nodeIds.add(c.getLong(NODE_ID))
        }

        return db.query(NAME, where = "$ID IN ($idsString)") { c ->
            val id = c.getLong(ID)
            OsmWay(
                id,
                c.getInt(VERSION),
                nodeIdsByWayId.getValue(id),
                c.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) },
                null,
                Date(c.getLong(TIMESTAMP))
            )
        }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.transaction {
            db.delete(NAME_NODES, "$ID IN ($idsString)")
            db.delete(NAME, "$ID IN ($idsString)")
        }
    }

    fun getAllForNode(nodeId: Long): List<Way> {
        val ids = db.query(
            NAME_NODES,
            columns = arrayOf(ID),
            where = "$NODE_ID = $nodeId"
        ) { it.getLong(ID) }.toSet()
        return getAll(ids)
    }

    fun getIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, columns = arrayOf(ID), where = "$LAST_SYNC < $timestamp") { it.getLong(ID) }
}
