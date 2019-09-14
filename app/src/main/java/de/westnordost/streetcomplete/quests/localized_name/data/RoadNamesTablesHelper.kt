package de.westnordost.streetcomplete.quests.localized_name.data

import android.database.sqlite.SQLiteDatabase

import de.westnordost.streetcomplete.data.TablesHelper

class RoadNamesTablesHelper : TablesHelper {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(RoadNamesTable.CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // was introduced in schema version 6
        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL(RoadNamesTable.CREATE)
        }
    }
}
