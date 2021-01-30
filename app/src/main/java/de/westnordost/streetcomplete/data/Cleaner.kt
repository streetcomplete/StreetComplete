package de.westnordost.streetcomplete.data

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Deletes old unused data in the background */
class Cleaner @Inject constructor(
    private val noteController: NoteController,
    private val osmElementController: OsmElementController,
    private val questTypeRegistry: QuestTypeRegistry
): CoroutineScope by CoroutineScope(Dispatchers.IO) {

    fun clean() {
        val timestamp = System.currentTimeMillis() - ApplicationConstants.DELETE_OLD_DATA_AFTER
        cleanNotes(timestamp)
        cleanElements(timestamp)
        cleanQuestMetadata(timestamp)
    }

    private fun cleanNotes(timestamp: Long) = launch {
        val time = System.currentTimeMillis()
        noteController.deleteAllOlderThan(timestamp)
        val seconds = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned notes in ${seconds.format(1)}s")
    }

    private fun cleanElements(timestamp: Long) = launch {
        val time = System.currentTimeMillis()
        osmElementController.deleteUnreferencedOlderThan(timestamp)
        val seconds = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned elements in ${seconds.format(1)}s")
    }

    private fun cleanQuestMetadata(timestamp: Long) = launch {
        val time = System.currentTimeMillis()
        for (questType in questTypeRegistry.all) {
            questType.deleteMetadataOlderThan(timestamp)
        }
        val seconds = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Cleaned quest metadata in ${seconds.format(1)}s")
    }

    companion object {
        private const val TAG = "Cleaner"
    }
}
