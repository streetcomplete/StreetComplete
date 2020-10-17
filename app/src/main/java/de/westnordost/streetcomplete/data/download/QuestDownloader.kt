package de.westnordost.streetcomplete.data.download

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestDownloader
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesDownloader
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.osmquest.OsmApiQuestDownloader
import de.westnordost.streetcomplete.data.user.UserStore
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.util.TilesRect
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max

/** Takes care of downloading all note and osm quests */
class QuestDownloader @Inject constructor(
    private val osmNotesDownloaderProvider: Provider<OsmNotesDownloader>,
    private val osmQuestDownloaderProvider: Provider<OsmQuestDownloader>,
    private val osmApiQuestDownloaderProvider: Provider<OsmApiQuestDownloader>,
    private val downloadedTilesDao: DownloadedTilesDao,
    private val questTypeRegistry: QuestTypeRegistry,
    private val questTypesProvider: OrderedVisibleQuestTypesProvider,
    private val userStore: UserStore
) {
    var progressListener: DownloadProgressListener? = null

    @Synchronized fun download(tiles: TilesRect, maxQuestTypes: Int?, cancelState: AtomicBoolean) {
        if (cancelState.get()) return

        progressListener?.onStarted()
        val questTypes = getQuestTypesToDownload(tiles, maxQuestTypes).toMutableList()
        if (questTypes.isEmpty()) {
            progressListener?.onSuccess()
            progressListener?.onFinished()
            return
        }

        val bbox = tiles.asBoundingBox(ApplicationConstants.QUEST_TILE_ZOOM)

        Log.i(TAG, "(${bbox.asLeftBottomRightTopString}) Starting")
        Log.i(TAG, "Quest types to download: ${questTypes.joinToString { it.javaClass.simpleName }}")

        try {
            downloadQuestTypes(tiles, bbox, questTypes, cancelState)
            progressListener?.onSuccess()
        } finally {
            progressListener?.onFinished()
            Log.i(TAG, "(${bbox.asLeftBottomRightTopString}) Finished")
        }
    }

    private fun downloadQuestTypes(
        tiles: TilesRect,
        bbox: BoundingBox,
        questTypes: MutableList<QuestType<*>>,
        cancelState: AtomicBoolean)
    {
        // always first download notes, because note positions are blockers for creating other
        // osm quests
        val noteQuestType = getOsmNoteQuestType()
        if (questTypes.remove(noteQuestType)) {
            downloadNotes(bbox, tiles)
        }

        if (questTypes.isEmpty()) return
        if (cancelState.get()) return

        // download multiple quests at once
        val downloadedQuestTypes = downloadMultipleOsmQuestTypes(bbox, tiles)
        questTypes.removeAll(downloadedQuestTypes)

        if (questTypes.isEmpty()) return
        if (cancelState.get()) return

        // download remaining quests that haven't been downloaded in the previous step
        val remainingOsmElementQuestTypes = questTypes.filterIsInstance<OsmElementQuestType<*>>()
        for (questType in remainingOsmElementQuestTypes) {
            if (cancelState.get()) break
            downloadOsmQuestType(bbox, tiles, questType)
            questTypes.remove(questType)
        }
    }

    private fun getOsmNoteQuestType() =
        questTypeRegistry.getByName(OsmNoteQuestType::class.java.simpleName)!!


    private fun getQuestTypesToDownload(tiles: TilesRect, maxQuestTypes: Int?): List<QuestType<*>> {
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

    private fun downloadNotes(bbox: BoundingBox, tiles: TilesRect) {
        val notesDownload = osmNotesDownloaderProvider.get()
        val userId: Long = userStore.userId.takeIf { it != -1L } ?: return
        // do not download notes if not logged in because notes shall only be downloaded if logged in
        val noteQuestType = getOsmNoteQuestType()
        progressListener?.onStarted(noteQuestType.toDownloadItem())
        val maxNotes = 10000
        notesDownload.download(bbox, userId, maxNotes)
        downloadedTilesDao.put(tiles, OsmNoteQuestType::class.java.simpleName)
        progressListener?.onFinished(noteQuestType.toDownloadItem())
    }

    private fun downloadOsmQuestType(bbox: BoundingBox, tiles: TilesRect, questType: OsmElementQuestType<*>) {
        progressListener?.onStarted(questType.toDownloadItem())
        val questDownload = osmQuestDownloaderProvider.get()
        if (questDownload.download(questType, bbox)) {
            downloadedTilesDao.put(tiles, questType.javaClass.simpleName)
        }
        progressListener?.onFinished(questType.toDownloadItem())
    }

    private fun downloadMultipleOsmQuestTypes(bbox: BoundingBox, tiles: TilesRect): List<OsmElementQuestType<*>> {
        val downloadItem = DownloadItem(R.drawable.ic_search_black_128dp, "Multi download")
        progressListener?.onStarted(downloadItem)
        // Since we query all the data at once, we can also do the downloading for quests not on our list.
        val questTypes = questTypesProvider.get().filterIsInstance<OsmElementQuestType<*>>()
        val questDownload = osmApiQuestDownloaderProvider.get()
        val downloadedQuestTypes = questDownload.download(questTypes, bbox)
        downloadedTilesDao.putAll(tiles, downloadedQuestTypes.map { it.javaClass.simpleName })
        progressListener?.onFinished(downloadItem)
        return downloadedQuestTypes
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}

private fun QuestType<*>.toDownloadItem(): DownloadItem = DownloadItem(icon, javaClass.simpleName)