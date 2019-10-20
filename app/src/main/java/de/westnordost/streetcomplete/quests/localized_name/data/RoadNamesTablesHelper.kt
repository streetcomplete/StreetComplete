package de.westnordost.streetcomplete.quests.localized_name.data

import android.database.sqlite.SQLiteDatabase

import de.westnordost.streetcomplete.data.TablesHelper

import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.GEOMETRY
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MAX_LATITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MAX_LONGITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MIN_LATITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.MIN_LONGITUDE
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.NAMES
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.Columns.WAY_ID
import de.westnordost.streetcomplete.quests.localized_name.data.RoadNamesTable.NAME

class RoadNamesTablesHelper : TablesHelper {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ROAD_NAMES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // was introduced in schema version 6
        if (oldVersion < 6 && newVersion >= 6) {
            db.execSQL(ROAD_NAMES)
        }
    }

    companion object {
        private const val ROAD_NAMES =
            "CREATE TABLE $NAME (" +
            "$WAY_ID int  PRIMARY KEY, " +
            "$NAMES blob NOT NULL, " +
            "$GEOMETRY blob NOT NULL, " +
            "$MIN_LATITUDE double NOT NULL, " +
            "$MIN_LONGITUDE double NOT NULL, " +
            "$MAX_LATITUDE double NOT NULL, " +
            "$MAX_LONGITUDE double NOT NULL " +
            ");"
    }
}
