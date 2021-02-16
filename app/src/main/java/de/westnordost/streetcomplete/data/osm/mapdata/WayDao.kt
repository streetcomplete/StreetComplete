package de.westnordost.streetcomplete.data.osm.mapdata

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.*

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.INDEX
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.NODE_ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables.NAME_NDS
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.util.Serializer2
import java.lang.System.currentTimeMillis

/** Stores OSM ways */
class WayDao @Inject constructor(
    private val dbHelper: SQLiteOpenHelper,
    private val serializer: Serializer2
) {
    private val db get() = dbHelper.writableDatabase

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
        db.transaction {
            db.delete(NAME_NDS, "$ID IN ($idsString)", null)
            for (way in ways) {
                way.nodeIds.forEachIndexed { index, nodeId ->
                    db.insertOrThrow(NAME_NDS, null, contentValuesOf(
                        ID to way.id,
                        NODE_ID to nodeId,
                        INDEX to index
                    ))
                }
                db.replaceOrThrow(NAME, null, contentValuesOf(
                    ID to way.id,
                    VERSION to way.version,
                    TAGS to way.tags?.let { serializer.encode(it) },
                    LAST_UPDATE to currentTimeMillis()
                ))
            }
        }
    }

    fun getAll(ids: Collection<Long>): List<Way> {
        if (ids.isEmpty()) return emptyList()
        val idsString = ids.joinToString(",")

        val nodeIdsByWayId = mutableMapOf<Long, MutableList<Long>>()
        db.query(NAME_NDS, selection = "$ID IN ($idsString", orderBy = "$ID, $INDEX") { c ->
            val nodeIds = nodeIdsByWayId.getOrPut(c.getLong(ID)) { ArrayList() }
            nodeIds.add(c.getLong(NODE_ID))
        }

        return db.query(NAME, selection = "$ID IN ($idsString)") { c ->
            val id = c.getLong(ID)
            OsmWay(
                id,
                c.getInt(VERSION),
                nodeIdsByWayId.getValue(id),
                c.getStringOrNull(TAGS)?.let { serializer.decode<HashMap<String, String>>(it) }
            )
        }
    }

    fun deleteAll(ids: Collection<Long>): Int {
        if (ids.isEmpty()) return 0
        val idsString = ids.joinToString(",")
        return db.transaction {
            db.delete(NAME_NDS, "$ID IN ($idsString)", null)
            db.delete(NAME, "$ID IN ($idsString)", null)
        }
    }

    fun getAllForNode(nodeId: Long): List<Way> {
        val ids = db.query(
            NAME_NDS,
            columns = arrayOf(ID),
            selection = "$NODE_ID = $nodeId"
        ) { it.getLong(0) }.toSet()
        return getAll(ids)
    }

    fun getIdsOlderThan(timestamp: Long): List<Long> =
        db.query(NAME, columns = arrayOf(ID), selection = "$LAST_UPDATE < $timestamp") { it.getLong(0) }
}
