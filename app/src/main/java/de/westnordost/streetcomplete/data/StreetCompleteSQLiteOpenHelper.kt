package de.westnordost.streetcomplete.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.user.achievements.UserAchievementsTable
import de.westnordost.streetcomplete.data.user.achievements.UserLinksTable

import javax.inject.Singleton

import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenChangesetsTable
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryTable
import de.westnordost.streetcomplete.data.osm.mapdata.NodeTable
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteTable
import de.westnordost.streetcomplete.data.osmnotes.NoteTable
import de.westnordost.streetcomplete.data.osm.mapdata.RelationTables
import de.westnordost.streetcomplete.data.osm.mapdata.WayTables
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeTable
import de.westnordost.streetcomplete.data.user.QuestStatisticsTable
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsTable
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsTable
import de.westnordost.streetcomplete.data.osm.changes.ElementIdProviderTable
import de.westnordost.streetcomplete.data.osm.osmquest.*
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteTable
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenTable
import de.westnordost.streetcomplete.data.user.CountryStatisticsTable
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable

@Singleton class StreetCompleteSQLiteOpenHelper(context: Context, dbName: String) :
    SQLiteOpenHelper(context, dbName, null, DB_VERSION) {

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // OSM notes
        db.execSQL(NoteTable.CREATE)

        // OSM map data
        db.execSQL(ElementGeometryTable.CREATE)
        db.execSQL(NodeTable.CREATE)
        db.execSQL(WayTables.CREATE)
        db.execSQL(WayTables.NODES_CREATE)
        db.execSQL(WayTables.NODES_INDEX_CREATE)
        db.execSQL(RelationTables.CREATE)
        db.execSQL(RelationTables.MEMBERS_CREATE)
        db.execSQL(RelationTables.MEMBERS_INDEX_CREATE)

        // changes made on OSM notes
        db.execSQL(CreateNoteTable.CREATE)
        db.execSQL(CommentNoteTable.CREATE)

        // changes made on OSM map data
        db.execSQL(ElementEditsTable.CREATE)
        db.execSQL(ElementIdProviderTable.CREATE)
        db.execSQL(ElementIdProviderTable.INDEX_CREATE)

        // quests
        db.execSQL(VisibleQuestTypeTable.CREATE)

        // quests based on OSM elements
        db.execSQL(OsmQuestTable.CREATE)
        db.execSQL(OsmQuestTable.MERGED_VIEW_CREATE)
        db.execSQL(OsmQuestsHiddenTable.CREATE)

        // quests based on OSM notes
        db.execSQL(NoteQuestsHiddenTable.CREATE)

        // for upload / download
        db.execSQL(OpenChangesetsTable.CREATE)
        db.execSQL(DownloadedTilesTable.CREATE)

        // user statistics
        db.execSQL(QuestStatisticsTable.CREATE)
        db.execSQL(CountryStatisticsTable.CREATE)
        db.execSQL(UserAchievementsTable.CREATE)
        db.execSQL(UserLinksTable.CREATE)
        db.execSQL(NewUserAchievementsTable.CREATE)

        // quest specific tables
        db.execSQL(WayTrafficFlowTable.CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // for later changes to the DB
        // ...
    }

}

private const val DB_VERSION = 1
