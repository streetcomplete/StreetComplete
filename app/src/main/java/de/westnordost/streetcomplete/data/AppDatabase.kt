package de.westnordost.streetcomplete.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.model.*

// The database version is incremented to preserve the existing data when migrating from SQLite.
@Database(
    entities = [
        ElementGeometry::class, OsmQuest::class, UndoOsmQuest::class, Node::class, Way::class
    ],
    version = 17
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase {
            var result = instance
            if (result == null) {
                synchronized(AppDatabase::class) {
                    result = instance
                    if (result == null) {
                        instance = Room.databaseBuilder(context, AppDatabase::class.java, ApplicationConstants.DATABASE_NAME)
                            .addMigrations(
                                MIGRATION_1_3, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                                MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                                MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                                MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17
                            )
                            .build()
                        result = instance
                    }
                }
            }
            return result!!
        }
    }
}
