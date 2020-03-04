package de.westnordost.streetcomplete.quests.oneway.data

import android.database.sqlite.SQLiteOpenHelper

import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.persist.WayTable
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.NAME
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.Columns.IS_FORWARD

import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.ktx.getInt
import de.westnordost.streetcomplete.ktx.queryOne

class WayTrafficFlowDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {

    private val db get() = dbHelper.writableDatabase

    fun put(wayId: Long, isForward: Boolean) {
        val contentValues = contentValuesOf(
            WAY_ID to wayId,
            IS_FORWARD to if (isForward) 1 else 0
        )

        db.replaceOrThrow(NAME, null, contentValues)
    }

    /** returns whether the direction of road user flow is forward or null if unknown
     */
    fun isForward(wayId: Long): Boolean? {
        val cols = arrayOf(IS_FORWARD)
        val query = "$WAY_ID = ?"
        val args = arrayOf(wayId.toString())

        return db.queryOne(NAME, cols, query, args) { it.getInt(IS_FORWARD) != 0 }
    }

    fun delete(wayId: Long) {
        val query = "$WAY_ID = ?"
        val args = arrayOf(wayId.toString())

        db.delete(NAME, query, args)
    }

    fun deleteUnreferenced() {
        val query = "$WAY_ID NOT IN (SELECT ${WayTable.Columns.ID} AS $WAY_ID FROM ${WayTable.NAME});"

        db.delete(NAME, query, null)
    }
}
