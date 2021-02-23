package de.westnordost.streetcomplete.data

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.changes.ElementEditsController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import javax.inject.Inject

/** Deletes old unused data in the background */
class Cleaner @Inject constructor(
    private val noteController: NoteController,
    private val mapDataController: MapDataController,
    private val elementEditsController: ElementEditsController,
    private val questTypeRegistry: QuestTypeRegistry
): CoroutineScope by CoroutineScope(Dispatchers.IO) {

    fun clean() {
        val oldDataTimestamp = currentTimeMillis() - ApplicationConstants.DELETE_OLD_DATA_AFTER
        cleanNotes(oldDataTimestamp)
        cleanElements(oldDataTimestamp)
        cleanQuestMetadata(oldDataTimestamp)

        val undoableChangesTimestamp = currentTimeMillis() - ApplicationConstants.MAX_UNDO_HISTORY_AGE
        cleanOldHistory(undoableChangesTimestamp)
    }

    private fun cleanNotes(timestamp: Long) = launch {
        val time = currentTimeMillis()
        noteController.deleteAllOlderThan(timestamp)
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned notes in ${seconds.format(1)}s")
    }

    private fun cleanElements(timestamp: Long) = launch {
        val time = currentTimeMillis()
        mapDataController.deleteOlderThan(timestamp)
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned elements in ${seconds.format(1)}s")
    }

    private fun cleanQuestMetadata(timestamp: Long) = launch {
        val time = currentTimeMillis()
        for (questType in questTypeRegistry.all) {
            questType.deleteMetadataOlderThan(timestamp)
        }
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned quest metadata in ${seconds.format(1)}s")
    }

    private fun cleanOldHistory(timestamp: Long) = launch {
        val time = currentTimeMillis()
        elementEditsController.deleteSyncedOlderThan(timestamp)
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned old history in ${seconds.format(1)}s")
    }

    companion object {
        private const val TAG = "Cleaner"
    }
}
