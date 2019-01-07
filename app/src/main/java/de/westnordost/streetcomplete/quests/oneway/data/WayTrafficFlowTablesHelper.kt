package de.westnordost.streetcomplete.quests.oneway.data

import android.database.sqlite.SQLiteDatabase

import de.westnordost.streetcomplete.data.TablesHelper
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.NAME
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.oneway.data.WayTrafficFlowTable.Columns.IS_FORWARD

class WayTrafficFlowTablesHelper : TablesHelper {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_WAY_TRAFFIC_FLOW)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // was introduced in schema version 10
        if (oldVersion < 10 && newVersion >= 10) {
            db.execSQL(CREATE_WAY_TRAFFIC_FLOW)
        }
        // all data was invalidated on version 11
        if (oldVersion < 11 && newVersion >= 11) {
            db.delete(WayTrafficFlowTable.NAME, null, null)
        }
    }

    companion object {
        private const val CREATE_WAY_TRAFFIC_FLOW =
            "CREATE TABLE $NAME ("+
            "$WAY_ID int PRIMARY KEY,"+
            "$IS_FORWARD int NOT NULL"+
            ");"
    }
}
