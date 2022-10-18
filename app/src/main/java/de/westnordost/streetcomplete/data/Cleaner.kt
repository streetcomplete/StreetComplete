package de.westnordost.streetcomplete.data

import android.util.Log
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Deletes old unused data in the background */
class Cleaner(
    private val noteController: NoteController,
    private val mapDataController: MapDataController,
    private val questTypeRegistry: QuestTypeRegistry
) {
    fun clean() {
        val time = nowAsEpochMilliseconds()

        val oldDataTimestamp = nowAsEpochMilliseconds() - ApplicationConstants.DELETE_OLD_DATA_AFTER
        noteController.deleteOlderThan(oldDataTimestamp, MAX_DELETE_ELEMENTS)
        mapDataController.deleteOlderThan(oldDataTimestamp, MAX_DELETE_ELEMENTS)
        /* do this after cleaning map data and notes, because some metadata rely on map data */
        questTypeRegistry.forEach { it.deleteMetadataOlderThan(oldDataTimestamp) }

        Log.i(TAG, "Cleaning took ${((nowAsEpochMilliseconds() - time) / 1000.0).format(1)}s")
    }

    companion object {
        private const val TAG = "Cleaner"

        /* Why deleting at most that many elements? Because I got crash reports of an out of memory
         * error in NodeDao.deleteAll: Some people managed to download so many OSM elements (in one
         * session) that Android is out of memory just joining all the ids that should be
         * deleted because they are too old to a string. 😐 */
        private const val MAX_DELETE_ELEMENTS = 100_000
    }
}
