package de.westnordost.streetcomplete.data

import android.content.Context
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesController
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.purge
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Deletes old unused persisted data in the background */
class Cleaner(
    private val context: Context,
    private val noteController: NoteController,
    private val mapDataController: MapDataController,
    private val questTypeRegistry: QuestTypeRegistry,
    private val downloadedTilesController: DownloadedTilesController,
    private val logsController: LogsController
) {
    private val scope = CoroutineScope(SupervisorJob() + CoroutineName("Cleaner") + Dispatchers.IO)

    fun cleanOld() = scope.launch {
        val time = nowAsEpochMilliseconds()

        val oldDataTimestamp = nowAsEpochMilliseconds() - ApplicationConstants.DELETE_OLD_DATA_AFTER
        noteController.deleteOlderThan(oldDataTimestamp, MAX_DELETE_ELEMENTS)
        mapDataController.deleteOlderThan(oldDataTimestamp, MAX_DELETE_ELEMENTS)
        downloadedTilesController.deleteOlderThan(oldDataTimestamp)
        // do this after cleaning map data and notes, because some metadata rely on map data
        questTypeRegistry.forEach { it.deleteMetadataOlderThan(oldDataTimestamp) }

        val oldLogTimestamp = nowAsEpochMilliseconds() - ApplicationConstants.DELETE_OLD_LOG_AFTER
        logsController.deleteOlderThan(oldLogTimestamp)

        Log.i(TAG, "Cleaning took ${((nowAsEpochMilliseconds() - time) / 1000.0).format(1)}s")
    }

    fun cleanAll() = scope.launch {
        context.externalCacheDir?.purge()
        downloadedTilesController.clear()
        mapDataController.clear()
        noteController.clear()
        logsController.clear()
        questTypeRegistry.forEach { it.deleteMetadataOlderThan(nowAsEpochMilliseconds()) }
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
