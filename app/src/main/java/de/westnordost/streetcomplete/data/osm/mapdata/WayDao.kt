package de.westnordost.streetcomplete.data.osm.mapdata

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.INDEX
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.LAST_SYNC
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.NODE_ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME_NODES
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Stores OSM ways */
class WayDao(private val db: Database) {
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

        val time = nowAsEpochMilliseconds()

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
                        if (way.tags.isNotEmpty()) jsonAdapter.toJson(way.tags) else null,
                        way.timestampEdited,
                        time
                    )
                }
            )
        }
    }

    fun getAll(ids: Collection<Long>): List<Way> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")

        return db.transaction {
            val nodeIdsByWayId = mutableMapOf<Long, MutableList<Long>>()
            db.query(NAME_NODES, where = "$ID IN ($idsString)", orderBy = "$ID, $INDEX", columns = arrayOf(ID, NODE_ID)) { c ->
                val nodeIds = nodeIdsByWayId.getOrPut(c.getLong(ID)) { ArrayList() }
                nodeIds.add(c.getLong(NODE_ID))
            }

            db.query(NAME, where = "$ID IN ($idsString)") { cursor ->
                Way(
                    cursor.getLong(ID),
                    nodeIdsByWayId.getValue(cursor.getLong(ID)),
                    cursor.getStringOrNull(TAGS)?.let { jsonAdapter.fromJson(it)?.let {
                        HashMap<String, String>(it.size, 1.0f).apply {
                            it.forEach { (k, v) -> put(k.intern(), v.intern()) }
                        }
                    } } ?: emptyMap(),
                    cursor.getInt(VERSION),
                    cursor.getLong(TIMESTAMP)
                )
            }
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

    fun clear() {
        db.transaction {
            db.delete(NAME_NODES)
            db.delete(NAME)
        }
    }

    fun getAllForNode(nodeId: Long): List<Way> =
        getAllForNodes(listOf(nodeId))

    // longer code, but 10-20% faster
    fun getAllForNodes(nodeIds: Collection<Long>): List<Way> {
        if (nodeIds.isEmpty()) return emptyList()
        val idsString = nodeIds.joinToString(",")

        return db.transaction {
            val nodeIdsByWayId = mutableMapOf<Long, MutableList<Long>>()
            db.query(NAME_NODES, where = "$ID IN (SELECT $ID FROM $NAME_NODES WHERE $NODE_ID IN ($idsString))", orderBy = "$ID, $INDEX", columns = arrayOf(ID, NODE_ID)) { c ->
                val nodeIds2 = nodeIdsByWayId.getOrPut(c.getLong(ID)) { ArrayList() }
                nodeIds2.add(c.getLong(NODE_ID))
            }

            db.query(NAME, where = "$ID IN (${nodeIdsByWayId.keys.joinToString(",")})") { cursor ->
                Way(
                    cursor.getLong(ID),
                    nodeIdsByWayId.getValue(cursor.getLong(ID)),
                    cursor.getStringOrNull(TAGS)?.let { jsonAdapter.fromJson(it)?.let { HashMap<String, String>(it.size, 1.0f).apply { putAll(it) } } }
                        ?: emptyMap(),
                    cursor.getInt(VERSION),
                    cursor.getLong(TIMESTAMP)
                )
            }
        }
    }

    fun getAllIdsForNodes(nodeIds: Collection<Long>): List<Long> {
        if (nodeIds.isEmpty()) return emptyList()
        val idsString = nodeIds.joinToString(",")
        return db.query(
            NAME_NODES,
            columns = arrayOf(ID),
            where = "$NODE_ID IN ($idsString)"
        ) { it.getLong(ID) }
    }

    fun getIdsOlderThan(timestamp: Long, limit: Int? = null): List<Long> {
        if (limit != null && limit <= 0) return emptyList()
        return db.query(NAME,
            columns = arrayOf(ID),
            where = "$LAST_SYNC < $timestamp",
            limit = limit?.toString()
        ) { it.getLong(ID) }
    }
}

private val jsonAdapter: JsonAdapter<Map<String, String>> = Moshi.Builder().build()
    .adapter(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
