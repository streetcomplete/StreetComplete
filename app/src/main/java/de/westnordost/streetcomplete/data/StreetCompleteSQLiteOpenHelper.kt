package de.westnordost.streetcomplete.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StreetCompleteSQLiteOpenHelper(context: Context, dbName: String) :
    SQLiteOpenHelper(context, dbName, null, DatabaseInitializer.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        DatabaseInitializer.onCreate(AndroidDatabase(db))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        DatabaseInitializer.onUpgrade(AndroidDatabase(db), oldVersion, newVersion)
    }
}
