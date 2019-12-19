package de.westnordost.streetcomplete.data.download

import android.content.SharedPreferences
import android.graphics.Rect
import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.QuestTypeRegistry
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.download.OsmQuestDownloader
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestDao
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesDownloader
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.util.SlippyMapMath
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max
import kotlin.math.min

class QuestDownloader @Inject constructor(
    private val osmNotesDownloaderProvider: Provider<OsmNotesDownloader>,
    private val osmQuestDownloaderProvider: Provider<OsmQuestDownloader>,
    private val downloadedTilesDao: DownloadedTilesDao,
    private val osmNoteQuestDb: OsmNoteQuestDao,
    private val questTypeRegistry: QuestTypeRegistry,
    private val prefs: SharedPreferences,
    private val questTypesProvider: OrderedVisibleQuestTypesProvider
) {
    // listeners
    var questListener: VisibleQuestListener? = null
    var progressListener: QuestDownloadProgressListener? = null

    // state
    private var downloadedQuestTypes = 0
    private var totalQuestTypes = 0

    @Synchronized fun download(tiles: Rect, maxQuestTypes: Int?, cancelState: AtomicBoolean) {
        if (cancelState.get()) return

        val questTypes = getQuestTypesToDownload(tiles, maxQuestTypes)
        if (questTypes.isEmpty()) {
            progressListener?.onNotStarted()
            return
        }

        totalQuestTypes = questTypes.size
        downloadedQuestTypes = 0

        val bbox = SlippyMapMath.asBoundingBox(tiles, ApplicationConstants.QUEST_TILE_ZOOM)

        Log.i(TAG, "(${bbox.asLeftBottomRightTopString}) Starting")
        Log.i(TAG, "Quest types to download: ${questTypes.joinToString { it.javaClass.simpleName }}")

        progressListener?.onStarted()
        try {
            val notesPositions =
                if (questTypes.contains(getOsmNoteQuestType())) downloadNotes(bbox, tiles)
                else osmNoteQuestDb.getAllPositions(bbox).toSet()

            for (questType in questTypes) {
                if (cancelState.get()) break
                downloadQuestType(bbox, tiles, questType, notesPositions)
            }
            progressListener?.onSuccess()
        } finally {
            progressListener?.onFinished()
            Log.i(TAG, "(${bbox.asLeftBottomRightTopString}) Finished")
        }
    }

    private fun getOsmNoteQuestType() =
        questTypeRegistry.getByName(OsmNoteQuestType::class.java.simpleName)!!

    private fun getQuestTypesToDownload(tiles: Rect, maxQuestTypes: Int?): List<QuestType<*>> {
        val result = questTypesProvider.get().toMutableList()
        val questExpirationTime = ApplicationConstants.REFRESH_QUESTS_AFTER
        val ignoreOlderThan = max(0, System.currentTimeMillis() - questExpirationTime)
        val alreadyDownloadedNames = downloadedTilesDao.get(tiles, ignoreOlderThan)
        if (alreadyDownloadedNames.isNotEmpty()) {
            val alreadyDownloaded = alreadyDownloadedNames.map { questTypeRegistry.getByName(it) }
            result.removeAll(alreadyDownloaded)
            Log.i(TAG, "Quest types already in local store: ${alreadyDownloadedNames.joinToString()}")
        }
        return if (maxQuestTypes != null && maxQuestTypes < result.size)
            result.subList(0, maxQuestTypes)
        else
            result
    }

    private fun downloadNotes(bbox: BoundingBox, tiles: Rect): Set<LatLon> {
        val notesDownload = osmNotesDownloaderProvider.get()
        notesDownload.questListener = questListener
        val userId: Long? = prefs.getLong(Prefs.OSM_USER_ID, -1L).takeIf { it != -1L }
        val maxNotes = 10000
        val result = notesDownload.download(bbox, userId, maxNotes)
        downloadedTilesDao.put(tiles, OsmNoteQuestType::class.java.simpleName)
        onProgress()
        return result
    }

    private fun downloadQuestType(bbox: BoundingBox, tiles: Rect, questType: QuestType<*>, notesPositions: Set<LatLon>) {
        if (questType is OsmElementQuestType<*>) {
            val questDownload = osmQuestDownloaderProvider.get()
            questDownload.questListener = questListener
            if (questDownload.download(questType, bbox, notesPositions)) {
                downloadedTilesDao.put(tiles, questType.javaClass.simpleName)
            }
            onProgress()
        }
    }

    private fun onProgress() {
        downloadedQuestTypes++
        progressListener?.onProgress(min(1f, downloadedQuestTypes.toFloat() / totalQuestTypes))
    }

    companion object {
        private const val TAG = "QuestDownload"
    }

}
