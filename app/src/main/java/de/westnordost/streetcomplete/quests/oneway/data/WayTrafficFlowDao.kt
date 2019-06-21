package de.westnordost.streetcomplete.quests.oneway.data

import android.database.sqlite.SQLiteOpenHelper

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.persist.WayTable
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.NAME
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.Columns.IS_FORWARD

import android.database.sqlite.SQLiteDatabase.*
import androidx.core.content.contentValuesOf

class WayTrafficFlowDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {

    fun put(wayId: Long, isForward: Boolean) {
        val values = contentValuesOf(
            WAY_ID to wayId,
            IS_FORWARD to if (isForward) 1 else 0
        )

        dbHelper.writableDatabase.insertWithOnConflict(NAME, null, values, CONFLICT_REPLACE)
    }

    /** returns whether the direction of road user flow is forward or null if unknown
     */
    fun isForward(wayId: Long): Boolean? {
        val cols = arrayOf(IS_FORWARD)
        val query = "$WAY_ID = ?"
        val args = arrayOf(wayId.toString())

        dbHelper.readableDatabase.query(NAME, cols, query, args, null, null, null, "1").use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) != 0
            }
        }
        return null
    }

    fun delete(wayId: Long) {
        val query = "$WAY_ID = ?"
        val args = arrayOf(wayId.toString())

        dbHelper.writableDatabase.delete(NAME, query, args)
    }

    fun deleteUnreferenced() {
        val query = "$WAY_ID NOT IN (SELECT ${WayTable.Columns.ID} AS $WAY_ID FROM ${WayTable.NAME});"

        dbHelper.writableDatabase.delete(NAME, query, null)
    }
}
