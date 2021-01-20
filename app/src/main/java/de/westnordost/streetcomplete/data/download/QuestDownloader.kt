package de.westnordost.streetcomplete.data.download

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.streetcomplete.data.osmnotes.OsmNotesDownloader
import de.westnordost.streetcomplete.data.osm.osmquest.*
import de.westnordost.streetcomplete.data.osm.osmquest.OsmApiQuestDownloader
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.UserStore
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider

/** Takes care of downloading all note and osm quests */
class QuestDownloader @Inject constructor(
    private val osmNotesDownloaderProvider: Provider<OsmNotesDownloader>,
    private val osmApiQuestDownloaderProvider: Provider<OsmApiQuestDownloader>,
    private val questTypeRegistry: QuestTypeRegistry,
    private val userStore: UserStore
) : Downloader {

    @Synchronized override fun download(bbox: BoundingBox, cancelState: AtomicBoolean) {
        if (cancelState.get()) return

        Log.i(TAG, "(${bbox.asLeftBottomRightTopString}) Starting")

        try {
            downloadQuestTypes(bbox, cancelState)
        } finally {
            Log.i(TAG, "(${bbox.asLeftBottomRightTopString}) Finished")
        }
    }

    private fun downloadQuestTypes(bbox: BoundingBox, cancelState: AtomicBoolean) {
        // always first download notes, note positions are blockers for creating other quests
        downloadNotes(bbox)

        if (cancelState.get()) return

        downloadOsmElementQuestTypes(bbox)
    }

    private fun downloadNotes(bbox: BoundingBox) {
        val notesDownload = osmNotesDownloaderProvider.get()
        val userId: Long = userStore.userId.takeIf { it != -1L } ?: return
        // do not download notes if not logged in because notes shall only be downloaded if logged in

        val maxNotes = 10000
        notesDownload.download(bbox, userId, maxNotes)
    }

    private fun downloadOsmElementQuestTypes(bbox: BoundingBox) {
        val questTypes = questTypeRegistry.all.filterIsInstance<OsmElementQuestType<*>>()
        osmApiQuestDownloaderProvider.get().download(questTypes, bbox)
    }

    companion object {
        private const val TAG = "QuestDownload"
    }
}
