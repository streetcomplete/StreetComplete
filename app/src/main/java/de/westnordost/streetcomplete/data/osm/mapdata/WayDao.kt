package de.westnordost.streetcomplete.data.osm.mapdata

import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.osmapi.map.data.Element

import javax.inject.Inject

import de.westnordost.streetcomplete.util.Serializer
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.ObjectRelationalMapping
import de.westnordost.streetcomplete.data.osm.mapdata.WayTable.Columns.ID
import de.westnordost.streetcomplete.data.osm.mapdata.WayTable.Columns.LAST_UPDATE
import de.westnordost.streetcomplete.data.osm.mapdata.WayTable.Columns.NODE_IDS
import de.westnordost.streetcomplete.data.osm.mapdata.WayTable.Columns.TAGS
import de.westnordost.streetcomplete.data.osm.mapdata.WayTable.Columns.VERSION
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayTable
import de.westnordost.streetcomplete.ktx.*
import java.util.*

/** Stores OSM ways */
class WayDao @Inject constructor(private val dbHelper: SQLiteOpenHelper, override val mapping: WayMapping)
    : AOsmElementDao<Way>(dbHelper) {

    private val db get() = dbHelper.writableDatabase

    override val tableName = WayTable.NAME
    override val idColumnName = ID
    override val lastUpdateColumnName = LAST_UPDATE
    override val elementTypeName = Element.Type.WAY.name

    override fun getUnusedAndOldIds(timestamp: Long): List<Long> {
        return db.query(tableName, arrayOf(idColumnName), """
            $lastUpdateColumnName < $timestamp AND
            $idColumnName NOT IN (
            $selectElementIdsInQuestTable
            UNION
            $selectElementIdsInUndoQuestTable
            UNION
            $selectElementIdsInDeleteElementsTable
            UNION
            SELECT ${SplitOsmWayTable.Columns.WAY_ID} AS $idColumnName FROM ${SplitOsmWayTable.NAME}
            )""".trimIndent()) {
            it.getLong(0)
        }
    }
}

class WayMapping @Inject constructor(private val serializer: Serializer)
    : ObjectRelationalMapping<Way> {

    override fun toContentValues(obj: Way) = contentValuesOf(
        ID to obj.id,
        VERSION to obj.version,
        NODE_IDS to serializer.toBytes(ArrayList(obj.nodeIds)),
        TAGS to obj.tags?.let { serializer.toBytes(HashMap(it)) },
        LAST_UPDATE to Date().time
    )

    override fun toObject(cursor: Cursor) = OsmWay(
        cursor.getLong(ID),
        cursor.getInt(VERSION),
        serializer.toObject<ArrayList<Long>>(cursor.getBlob(NODE_IDS)),
        cursor.getBlobOrNull(TAGS)?.let { serializer.toObject<HashMap<String, String>>(it) }
    )
}
