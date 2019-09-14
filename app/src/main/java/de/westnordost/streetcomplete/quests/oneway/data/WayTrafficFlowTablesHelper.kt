package de.westnordost.streetcomplete.quests.oneway.data

import android.database.sqlite.SQLiteDatabase

import de.westnordost.streetcomplete.data.TablesHelper
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.NAME

class WayTrafficFlowTablesHelper : TablesHelper {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(WayTrafficFlowTable.CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // was introduced in schema version 10
        if (oldVersion < 10 && newVersion >= 10) {
            db.execSQL(WayTrafficFlowTable.CREATE)
        }
        // all data was invalidated on version 11
        if (oldVersion < 11 && newVersion >= 11) {
            db.delete(NAME, null, null)
        }
    }
}
