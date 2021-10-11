package de.westnordost.streetcomplete.data

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.notifications.NewUserAchievementsDao
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/** Deletes old unused data in the background */
class Cleaner @Inject constructor(
    private val noteController: NoteController,
    private val mapDataController: MapDataController,
    private val elementEditsController: ElementEditsController,
    private val noteEditsController: NoteEditsController,
    private val questTypeRegistry: QuestTypeRegistry,
    private val newUserAchievementsDao: NewUserAchievementsDao
) {
    suspend fun clean() {
        val time = currentTimeMillis()
        coroutineScope {
            launch { deleteOldData() }
            launch { deleteUndoHistory() }
            launch { clearNewUserAchievements() }
        }
        Log.i(TAG, "Cleaning took ${((currentTimeMillis() - time) / 1000.0).format(1)}s")
    }

    private suspend fun deleteOldData() = withContext(Dispatchers.IO) {
        val oldDataTimestamp = currentTimeMillis() - ApplicationConstants.DELETE_OLD_DATA_AFTER
        noteController.deleteAllOlderThan(oldDataTimestamp)
        mapDataController.deleteOlderThan(oldDataTimestamp)
        /* it makes sense to do this after cleaning map data and notes, because some metadata rely
           on map data */
        for (questType in questTypeRegistry) {
            questType.deleteMetadataOlderThan(oldDataTimestamp)
        }
    }

    private suspend fun deleteUndoHistory() = withContext(Dispatchers.IO) {
        val undoableChangesTimestamp = currentTimeMillis() - ApplicationConstants.MAX_UNDO_HISTORY_AGE
        elementEditsController.deleteSyncedOlderThan(undoableChangesTimestamp)
        noteEditsController.deleteSyncedOlderThan(undoableChangesTimestamp)
    }

    private suspend fun clearNewUserAchievements() = withContext(Dispatchers.IO) {
        newUserAchievementsDao.clear()
    }

    companion object {
        private const val TAG = "Cleaner"
    }
}
