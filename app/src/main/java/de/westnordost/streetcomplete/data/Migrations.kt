package de.westnordost.streetcomplete.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsTable
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTable
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestTable
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestTable
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayTable
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenChangesetsTable
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestTable
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable
import de.westnordost.streetcomplete.data.visiblequests.QuestVisibilityTable
import de.westnordost.streetcomplete.ktx.hasColumn
import de.westnordost.streetcomplete.quests.oneway_suspects.AddSuspectedOneway
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable
import de.westnordost.streetcomplete.quests.road_name.data.RoadNamesTable

val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(OpenChangesetsTable.CREATE)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val tableName = OsmQuestTable.NAME
        val oldTableName = tableName + "_old"
        database.execSQL("ALTER TABLE $tableName RENAME TO $oldTableName")
        database.execSQL(OsmQuestTable.CREATE_DB_VERSION_3)
        val allColumns = OsmQuestTable.ALL_COLUMNS_DB_VERSION_3.joinToString(",")
        database.execSQL("INSERT INTO $tableName ($allColumns) SELECT $allColumns FROM $oldTableName")
        database.execSQL("DROP TABLE $oldTableName")

        database.execSQL(OpenChangesetsTable.CREATE)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        if (!database.hasColumn(OsmQuestTable.NAME, OsmQuestTable.Columns.CHANGES_SOURCE)) {
            database.execSQL("""
                ALTER TABLE ${OsmQuestTable.NAME}
                ADD COLUMN ${OsmQuestTable.Columns.CHANGES_SOURCE} varchar(255);
                """.trimIndent()
            )
        }
        database.execSQL("""
            UPDATE ${OsmQuestTable.NAME}
            SET ${OsmQuestTable.Columns.CHANGES_SOURCE} = 'survey'
            WHERE ${OsmQuestTable.Columns.CHANGES_SOURCE} ISNULL;
            """.trimIndent()
        )

        // sqlite does not support dropping/altering constraints. Need to create new table.
        // For simplicity sake, we just drop the old table and create it anew, this has the
        // effect that all currently open changesets will not be used but instead new ones are
        // created. That's okay because OSM server closes open changesets after 1h automatically.
        database.execSQL("DROP TABLE ${OpenChangesetsTable.NAME};")
        database.execSQL(OpenChangesetsTable.CREATE)
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE ${CreateNoteTable.NAME}
            ADD COLUMN ${CreateNoteTable.Columns.QUEST_TITLE} text;
            """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(RoadNamesTable.CREATE)
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(UndoOsmQuestTable.CREATE)
        database.execSQL(UndoOsmQuestTable.MERGED_VIEW_CREATE)
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE ${CreateNoteTable.NAME}
            ADD COLUMN ${CreateNoteTable.Columns.IMAGE_PATHS} blob;
            """.trimIndent()
        )
        database.execSQL("""
            ALTER TABLE ${OsmNoteQuestTable.NAME}
            ADD COLUMN ${OsmNoteQuestTable.Columns.IMAGE_PATHS} blob;
            """.trimIndent()
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(QuestVisibilityTable.CREATE)
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(WayTrafficFlowTable.CREATE)
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        val where = OsmQuestTable.Columns.QUEST_TYPE + " = ?"
        val args = arrayOf(AddSuspectedOneway::class.java.simpleName)
        database.delete(OsmQuestTable.NAME, where, args)
        database.delete(UndoOsmQuestTable.NAME, where, args)
        database.delete(WayTrafficFlowTable.NAME, null, null)
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(OsmQuestSplitWayTable.CREATE)
        // slightly different structure for undo osm quest table. Isn't worth converting
        database.execSQL("DROP TABLE ${UndoOsmQuestTable.NAME}")
        database.execSQL("DROP VIEW ${UndoOsmQuestTable.NAME_MERGED_VIEW}")
        database.execSQL(UndoOsmQuestTable.CREATE)
        database.execSQL(UndoOsmQuestTable.MERGED_VIEW_CREATE)
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(UserAchievementsTable.CREATE)
        database.execSQL(UserLinksTable.CREATE)
        database.execSQL(NewUserAchievementsTable.CREATE)
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(CountryStatisticsTable.CREATE)
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE ${OsmQuestSplitWayTable.NAME}
            ADD COLUMN ${OsmQuestSplitWayTable.Columns.QUEST_TYPES_ON_WAY} text;
            """.trimIndent()
        )
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        /* there was an indication that relations downloaded and serialized with v22.0-beta1 and
           v22.0 might have corrupt relation members. So to be on the safe side, we better clean
           ALL the relations currently in the store. See #2014
         */
        database.execSQL("""
            DELETE FROM ${OsmQuestTable.NAME}
            WHERE ${OsmQuestTable.Columns.ELEMENT_TYPE} = "${Element.Type.RELATION.name}"
        """.trimIndent())
        database.execSQL("""
            DELETE FROM ${UndoOsmQuestTable.NAME}
            WHERE ${UndoOsmQuestTable.Columns.ELEMENT_TYPE} = "${Element.Type.RELATION.name}"
        """.trimIndent())
        database.execSQL("""
            DELETE FROM ${RelationTable.NAME}
        """.trimIndent())
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // An empty migration tells Room to keep the existing data.
    }
}
