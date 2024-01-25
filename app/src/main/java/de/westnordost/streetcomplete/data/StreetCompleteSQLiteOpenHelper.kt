package de.westnordost.streetcomplete.data

import android.content.Context
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteOpenHelper
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable
import de.westnordost.streetcomplete.quests.osmose.OsmoseTable
import de.westnordost.streetcomplete.data.externalsource.ExternalSourceQuestTables

class StreetCompleteSQLiteOpenHelper(context: Context, dbName: String) :
    SQLiteOpenHelper(context, dbName, null, DatabaseInitializer.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        DatabaseInitializer.onCreate(AndroidDatabase(db))

        // external source and osmose tables (get created in init as well, but apparently may still cause issues)
        db.execSQL(ExternalSourceQuestTables.CREATE_HIDDEN)
        db.execSQL(ExternalSourceQuestTables.CREATE_EDITS)
        db.execSQL(OsmoseTable.CREATE_IF_NOT_EXISTS)
        db.execSQL(OsmoseTable.CREATE_SPATIAL_INDEX_IF_NOT_EXISTS)
    }

    init {
        // create some EE tables if not existing
        // this is to avoid actual db upgrade to keep compatibility with upstream

        // create other source tables
        writableDatabase.execSQL(ExternalSourceQuestTables.CREATE_HIDDEN)
        writableDatabase.execSQL(ExternalSourceQuestTables.CREATE_EDITS)

        // create osmose table
        writableDatabase.execSQL(OsmoseTable.CREATE_IF_NOT_EXISTS)
        writableDatabase.execSQL(OsmoseTable.CREATE_SPATIAL_INDEX_IF_NOT_EXISTS)
        // create osm quests element id index if not existing
        writableDatabase.execSQL(OsmQuestTable.CREATE_ELEMENT_ID_INDEX_IF_NOT_EXISTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        DatabaseInitializer.onUpgrade(AndroidDatabase(db), oldVersion, newVersion)
    }
}
