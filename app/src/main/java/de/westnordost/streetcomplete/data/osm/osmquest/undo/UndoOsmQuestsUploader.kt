package de.westnordost.streetcomplete.data.osm.osmquest.undo

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementUpdateController
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.data.osm.osmquest.SingleOsmElementTagChangesUploader
import de.westnordost.streetcomplete.data.user.StatisticsUpdater
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all undo osm quests from local DB and uploads them via the OSM API */
class UndoOsmQuestsUploader @Inject constructor(
    changesetManager: OpenQuestChangesetsManager,
    elementUpdateController: OsmElementUpdateController,
    private val undoQuestDB: UndoOsmQuestDao,
    private val singleChangeUploader: SingleOsmElementTagChangesUploader,
    private val statisticsUpdater: StatisticsUpdater
) : OsmInChangesetsUploader<UndoOsmQuest>(changesetManager, elementUpdateController) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Undoing quest changes")
        super.upload(cancelled)
    }

    override fun getAll() = undoQuestDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: UndoOsmQuest, element: Element): List<Element> {
        return listOf(singleChangeUploader.upload(changesetId, quest, element))
    }

    override fun onUploadSuccessful(quest: UndoOsmQuest) {
        undoQuestDB.delete(quest.id!!)
        statisticsUpdater.subtractOne(quest.osmElementQuestType.javaClass.simpleName, quest.position)
        Log.d(TAG, "Uploaded undo osm quest ${quest.toLogString()}")

    }

    override fun onUploadFailed(quest: UndoOsmQuest, e: Throwable) {
        undoQuestDB.delete(quest.id!!)
        Log.d(TAG, "Dropped undo osm quest ${quest.toLogString()}: ${e.message}")
    }

    companion object {
        private const val TAG = "UndoOsmQuestUpload"
    }
}

private fun UndoOsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId

