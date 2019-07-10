package de.westnordost.streetcomplete.data.osm.persist

import android.content.ContentValues
import android.database.Cursor
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.data.StreetCompleteOpenHelper
import de.westnordost.streetcomplete.data.osm.changes.SplitWayAtPosition
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.DELTA
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.NAME
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.WAY_ID
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE1_ID
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE1_LAT
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE1_LON
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE1_VERSION
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE2_ID
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE2_LAT
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE2_LON
import de.westnordost.streetcomplete.data.osm.persist.SplitWayTable.Columns.NODE2_VERSION
import de.westnordost.streetcomplete.ktx.*
import javax.inject.Inject

class SplitWayDao @Inject constructor(
    private val dbHelper: StreetCompleteOpenHelper) {

    fun get(wayId: Long): List<SplitWayAtPosition> {
        return dbHelper.readableDatabase.query(NAME, null, "$WAY_ID = $wayId", null).use { cursor ->
            val result = mutableListOf<SplitWayAtPosition>()
            while (cursor.moveToNext()) {
                result.add(cursor.createSplitWayAtPosition())
            }
            return result
        }
    }

    fun put(wayId: Long, splits: List<SplitWayAtPosition>) {
        dbHelper.writableDatabase.transaction {
            val values = ContentValues()
            for (split in splits) {
                values.put(WAY_ID, wayId)
                values.add(split)
                insert(NAME, null, values)
                values.clear()
            }
        }
    }

    fun delete(wayId: Long): Int {
        return dbHelper.writableDatabase.delete(NAME, "$WAY_ID = $wayId", null)
    }

    private fun ContentValues.add(split: SplitWayAtPosition) {
        put(NODE1_ID, split.firstNode.id)
        put(NODE1_VERSION, split.firstNode.version.toLong())
        put(NODE1_LAT, split.firstNode.position.latitude)
        put(NODE1_LON, split.firstNode.position.longitude)
        put(NODE2_ID, split.secondNode.id)
        put(NODE2_VERSION, split.secondNode.version.toLong())
        put(NODE2_LAT, split.secondNode.position.latitude)
        put(NODE2_LON, split.secondNode.position.longitude)
        put(DELTA, split.delta)
    }

    private fun Cursor.createSplitWayAtPosition() = SplitWayAtPosition(
        OsmNode(getLong(NODE1_ID), getInt(NODE1_VERSION), getDouble(NODE1_LAT), getDouble(NODE1_LON), null),
        OsmNode(getLong(NODE2_ID), getInt(NODE2_VERSION), getDouble(NODE2_LAT), getDouble(NODE2_LON), null),
        getDouble(DELTA)
    )
}
