package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable
import de.westnordost.streetcomplete.data.logs.LogsTable
import de.westnordost.streetcomplete.data.osm.created_elements.CreatedElementsTable
import de.westnordost.streetcomplete.data.osm.edits.EditElementsTable
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsTable
import de.westnordost.streetcomplete.data.osm.edits.ElementIdProviderTable
import de.westnordost.streetcomplete.data.osm.edits.upload.changesets.OpenChangesetsTable
import de.westnordost.streetcomplete.data.osm.geometry.RelationGeometryTable
import de.westnordost.streetcomplete.data.osm.geometry.WayGeometryTable
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestTable
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenTable
import de.westnordost.streetcomplete.data.osmnotes.NoteTable
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable
import de.westnordost.streetcomplete.data.user.statistics.ActiveDaysTable
import de.westnordost.streetcomplete.data.user.statistics.CountryStatisticsTables
import de.westnordost.streetcomplete.data.user.statistics.EditTypeStatisticsTables
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsTable
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderTable
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable

/** Creates the database and upgrades it */
object DatabaseInitializer {
    const val DB_VERSION = 19

    fun onCreate(db: Database) {
        // OSM notes
        db.exec(NoteTable.CREATE)
        db.exec(NoteTable.SPATIAL_INDEX_CREATE)

        // changes made on OSM notes
        db.exec(NoteEditsTable.CREATE)
        db.exec(NoteEditsTable.SPATIAL_INDEX_CREATE)
        db.exec(NoteEditsTable.NOTE_ID_INDEX_CREATE)

        // OSM map data
        db.exec(WayGeometryTable.CREATE)
        db.exec(RelationGeometryTable.CREATE)

        db.exec(NodeTable.CREATE)
        db.exec(NodeTable.SPATIAL_INDEX_CREATE)

        db.exec(WayTables.CREATE)
        db.exec(WayTables.NODES_CREATE)
        db.exec(WayTables.NODES_INDEX_CREATE)
        db.exec(WayTables.WAYS_BY_NODE_ID_INDEX_CREATE)

        db.exec(RelationTables.CREATE)
        db.exec(RelationTables.MEMBERS_CREATE)
        db.exec(RelationTables.MEMBERS_INDEX_CREATE)
        db.exec(RelationTables.MEMBERS_BY_ELEMENT_INDEX_CREATE)

        // changes made on OSM map data
        db.exec(ElementEditsTable.CREATE)
        db.exec(ElementIdProviderTable.CREATE)
        db.exec(ElementIdProviderTable.INDEX_CREATE)
        db.exec(ElementIdProviderTable.ELEMENT_INDEX_CREATE)

        db.exec(EditElementsTable.CREATE)
        db.exec(EditElementsTable.INDEX_CREATE)

        db.exec(CreatedElementsTable.CREATE)

        // quests
        db.exec(VisibleQuestTypeTable.CREATE)
        db.exec(QuestTypeOrderTable.CREATE)
        db.exec(QuestTypeOrderTable.INDEX_CREATE)
        db.exec(QuestPresetsTable.CREATE)

        // quests based on OSM elements
        db.exec(OsmQuestTable.CREATE)
        db.exec(OsmQuestTable.SPATIAL_INDEX_CREATE)
        db.exec(OsmQuestsHiddenTable.CREATE)

        // quests based on OSM notes
        db.exec(NoteQuestsHiddenTable.CREATE)

        // for upload / download
        db.exec(OpenChangesetsTable.CREATE)
        db.exec(DownloadedTilesTable.CREATE)

        // user statistics
        db.exec(EditTypeStatisticsTables.create(EditTypeStatisticsTables.NAME))
        db.exec(EditTypeStatisticsTables.create(EditTypeStatisticsTables.NAME_CURRENT_WEEK))
        db.exec(CountryStatisticsTables.create(CountryStatisticsTables.NAME))
        db.exec(CountryStatisticsTables.create(CountryStatisticsTables.NAME_CURRENT_WEEK))
        db.exec(UserAchievementsTable.CREATE)
        db.exec(UserLinksTable.CREATE)
        db.exec(ActiveDaysTable.CREATE)

        // logs
        db.exec(LogsTable.CREATE)
        db.exec(LogsTable.INDEX_CREATE)
    }

    fun onUpgrade(db: Database, oldVersion: Int, newVersion: Int) {
        if (oldVersion <= 1 && newVersion > 1) {
            db.exec(CreatedElementsTable.CREATE)
        }
        if (oldVersion <= 2 && newVersion > 2) {
            db.exec(QuestTypeOrderTable.CREATE)
            db.exec(QuestTypeOrderTable.INDEX_CREATE)

            db.exec(QuestPresetsTable.CREATE)

            val oldName = "quest_visibility_old"
            db.exec("ALTER TABLE ${VisibleQuestTypeTable.NAME} RENAME TO $oldName;")
            db.exec(VisibleQuestTypeTable.CREATE)
            db.exec("""
                INSERT INTO ${VisibleQuestTypeTable.NAME} (
                    ${VisibleQuestTypeTable.Columns.QUEST_PRESET_ID},
                    ${VisibleQuestTypeTable.Columns.QUEST_TYPE},
                    ${VisibleQuestTypeTable.Columns.VISIBILITY}
                ) SELECT
                    0,
                    ${VisibleQuestTypeTable.Columns.QUEST_TYPE},
                    ${VisibleQuestTypeTable.Columns.VISIBILITY}
                FROM $oldName;
            """.trimIndent())
            db.exec("DROP TABLE $oldName;")
        }
        if (oldVersion <= 3 && newVersion > 3) {
            db.exec("DROP TABLE new_achievements")
        }
        if (oldVersion <= 4 && newVersion > 4) {
            db.exec(NodeTable.SPATIAL_INDEX_CREATE)
            db.exec(WayGeometryTable.CREATE)
            db.exec(RelationGeometryTable.CREATE)
            val oldGeometryTableName = "elements_geometry"
            val oldTypeName = "element_type"
            val oldIdName = "element_id"
            db.exec("""
                INSERT INTO ${WayGeometryTable.NAME} (
                    ${WayGeometryTable.Columns.ID},
                    ${WayGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${WayGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${WayGeometryTable.Columns.CENTER_LATITUDE},
                    ${WayGeometryTable.Columns.CENTER_LONGITUDE}
                ) SELECT
                    $oldIdName,
                    ${WayGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${WayGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${WayGeometryTable.Columns.CENTER_LATITUDE},
                    ${WayGeometryTable.Columns.CENTER_LONGITUDE}
                FROM
                    $oldGeometryTableName
                WHERE
                    $oldTypeName = 'WAY';
            """.trimIndent()
            )
            db.exec("""
                INSERT INTO ${RelationGeometryTable.NAME} (
                    ${RelationGeometryTable.Columns.ID},
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${RelationGeometryTable.Columns.CENTER_LATITUDE},
                    ${RelationGeometryTable.Columns.CENTER_LONGITUDE}
                ) SELECT
                    $oldIdName,
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYLINES},
                    ${RelationGeometryTable.Columns.GEOMETRY_POLYGONS},
                    ${RelationGeometryTable.Columns.CENTER_LATITUDE},
                    ${RelationGeometryTable.Columns.CENTER_LONGITUDE}
                FROM
                    $oldGeometryTableName
                WHERE
                    $oldTypeName = 'RELATION';
            """.trimIndent()
            )
            db.exec("DROP TABLE $oldGeometryTableName;")
        }
        if (oldVersion <= 5 && newVersion > 5) {
            db.exec("ALTER TABLE ${NoteEditsTable.NAME} ADD COLUMN ${NoteEditsTable.Columns.TRACK} text DEFAULT '[]' NOT NULL")
        }
        if (oldVersion <= 6 && newVersion > 6) {
            db.exec(EditTypeStatisticsTables.create(EditTypeStatisticsTables.NAME_CURRENT_WEEK))
            db.exec(CountryStatisticsTables.create(CountryStatisticsTables.NAME_CURRENT_WEEK))
            db.exec(ActiveDaysTable.CREATE)
        }
        if (oldVersion <= 7 && newVersion > 7) {
            db.delete(ElementEditsTable.NAME, "${ElementEditsTable.Columns.QUEST_TYPE} = 'AddShoulder'", null)
        }
        if (oldVersion <= 8 && newVersion > 8) {
            db.renameQuest("AddPicnicTableCover", "AddAmenityCover")
        }
        if (oldVersion <= 9 && newVersion > 9) {
            db.exec("DROP TABLE ${DownloadedTilesTable.NAME};")
            db.exec(DownloadedTilesTable.CREATE)
        }
        if (oldVersion <= 10 && newVersion > 10) {
            db.exec("DROP INDEX osm_element_edits_index")

            // Recreating table (=clearing table) because it would be very complicated to pick the
            // data from the table in the old format and put it into the new format: the fields of
            // the serialized actions all changed
            db.exec("DROP TABLE ${ElementEditsTable.NAME};")
            db.exec(ElementEditsTable.CREATE)

            db.exec(EditElementsTable.CREATE)
            db.exec(EditElementsTable.INDEX_CREATE)

            db.exec(ElementIdProviderTable.ELEMENT_INDEX_CREATE)
        }
        if (oldVersion <= 11 && newVersion > 11) {
            db.exec(LogsTable.CREATE)
            db.exec(LogsTable.INDEX_CREATE)
        }
        if (oldVersion <= 12 && newVersion > 12) {
            // Deleting all data related to elements and their geometries, since
            // the binary format in which `GEOMETRY_{POLYGONS,POLYLINES}` data from
            // `{Way,Relation}GeometryTable` is stored has changed (see #5307)

            db.exec("DELETE FROM ${RelationTables.NAME_MEMBERS};")
            db.exec("DELETE FROM ${RelationTables.NAME};")

            db.exec("DELETE FROM ${WayTables.NAME_NODES};")
            db.exec("DELETE FROM ${WayTables.NAME};")

            db.exec("DELETE FROM ${NodeTable.NAME};")

            db.exec("DELETE FROM ${RelationGeometryTable.NAME};")
            db.exec("DELETE FROM ${WayGeometryTable.NAME};")

            db.exec("DELETE FROM ${DownloadedTilesTable.NAME};")
        }
        if (oldVersion <= 13 && newVersion > 13) {
            db.exec("ALTER TABLE ${ElementEditsTable.NAME} ADD COLUMN ${ElementEditsTable.Columns.IS_NEAR_USER_LOCATION} int DEFAULT 1 NOT NULL")
            db.exec("ALTER TABLE ${OpenChangesetsTable.NAME} ADD COLUMN ${OpenChangesetsTable.Columns.LAST_POSITION_LATITUDE} double DEFAULT 0 NOT NULL")
            db.exec("ALTER TABLE ${OpenChangesetsTable.NAME} ADD COLUMN ${OpenChangesetsTable.Columns.LAST_POSITION_LONGITUDE} double DEFAULT 0 NOT NULL")
        }
        if (oldVersion <= 14 && newVersion > 14) {
            db.renameOverlay("ShopsOverlay", "PlacesOverlay")
        }
        if (oldVersion <= 15 && newVersion > 15) {
            db.deleteQuest("AddCrossingType")
        }
        if (oldVersion <= 16 && newVersion > 16) {
            db.renameQuest("AddProhibitedForMoped", "AddMopedAccess")
        }
        if (oldVersion <= 17 && newVersion > 17) {
            db.exec("DROP TABLE direction_of_flow;")
            db.deleteQuest("AddSuspectedOneway")
        }
        if (oldVersion <= 18 && newVersion > 18) {
            db.deleteQuest("AddParcelLockerMailIn")
            db.deleteQuest("AddParcelLockerPickup")
            db.deleteQuest("AddShoulder")
        }
    }
}

private fun Database.deleteQuest(name: String) {
    deleteValue(ElementEditsTable.NAME, ElementEditsTable.Columns.QUEST_TYPE, name)
    deleteValue(OsmQuestTable.NAME, OsmQuestTable.Columns.QUEST_TYPE, name)
    deleteValue(OsmQuestsHiddenTable.NAME, OsmQuestsHiddenTable.Columns.QUEST_TYPE, name)
    deleteValue(VisibleQuestTypeTable.NAME, VisibleQuestTypeTable.Columns.QUEST_TYPE, name)
    deleteValue(OpenChangesetsTable.NAME, OpenChangesetsTable.Columns.QUEST_TYPE, name)
    deleteValue(QuestTypeOrderTable.NAME, QuestTypeOrderTable.Columns.BEFORE, name)
    deleteValue(QuestTypeOrderTable.NAME, QuestTypeOrderTable.Columns.AFTER, name)
}

private fun Database.renameQuest(old: String, new: String) {
    renameValue(ElementEditsTable.NAME, ElementEditsTable.Columns.QUEST_TYPE, old, new)
    renameValue(OsmQuestTable.NAME, OsmQuestTable.Columns.QUEST_TYPE, old, new)
    renameValue(OsmQuestsHiddenTable.NAME, OsmQuestsHiddenTable.Columns.QUEST_TYPE, old, new)
    renameValue(VisibleQuestTypeTable.NAME, VisibleQuestTypeTable.Columns.QUEST_TYPE, old, new)
    renameValue(OpenChangesetsTable.NAME, OpenChangesetsTable.Columns.QUEST_TYPE, old, new)
    renameValue(QuestTypeOrderTable.NAME, QuestTypeOrderTable.Columns.BEFORE, old, new)
    renameValue(QuestTypeOrderTable.NAME, QuestTypeOrderTable.Columns.AFTER, old, new)
}

private fun Database.renameOverlay(old: String, new: String) {
    renameValue(ElementEditsTable.NAME, ElementEditsTable.Columns.QUEST_TYPE, old, new)
    renameValue(OpenChangesetsTable.NAME, OpenChangesetsTable.Columns.QUEST_TYPE, old, new)
}

private fun Database.renameValue(table: String, column: String, oldValue: String, newValue: String) {
    update(table, listOf(column to newValue), "$column = ?", arrayOf(oldValue))
}

private fun Database.deleteValue(table: String, column: String, value: String) {
    delete(table, "$column = ?", arrayOf(value))
}
