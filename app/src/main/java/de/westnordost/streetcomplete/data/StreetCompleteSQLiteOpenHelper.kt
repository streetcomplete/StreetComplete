package de.westnordost.streetcomplete.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
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
import de.westnordost.streetcomplete.quests.oneway_suspects.data.WayTrafficFlowTable

class StreetCompleteSQLiteOpenHelper(context: Context, dbName: String) :
    SQLiteOpenHelper(context, dbName, null, DatabaseInitializer.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        DatabaseInitializer.onCreate(AndroidDatabase(db))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        DatabaseInitializer.onUpgrade(AndroidDatabase(db), oldVersion, newVersion)
    }
}
