package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.UndoOsmQuest
import de.westnordost.streetcomplete.data.osm.download.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import javax.inject.Inject

import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all undo osm quests from local DB and uploads them via the OSM API */
class UndoOsmQuestsUploader @Inject constructor(
    elementDB: MergedElementDao,
    elementGeometryDB: ElementGeometryDao,
    changesetManager: OpenQuestChangesetsManager,
    questGiver: OsmQuestGiver,
    statisticsDB: QuestStatisticsDao,
    osmApiElementGeometryCreator: OsmApiElementGeometryCreator,
    private val undoQuestDB: UndoOsmQuestDao,
    private val singleChangeUpload: SingleOsmElementTagChangesUpload
) : OsmInChangesetsUploader<UndoOsmQuest>(elementDB, elementGeometryDB, changesetManager, questGiver,
    statisticsDB, osmApiElementGeometryCreator) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Undoing quest changes")
        super.upload(cancelled)
    }

    override fun getAll() = undoQuestDB.getAll()

    override fun uploadSingle(changesetId: Long, quest: UndoOsmQuest, element: Element): List<Element> {
        return listOf(singleChangeUpload.upload(changesetId, quest, element))
    }

    override fun onUploadSuccessful(quest: UndoOsmQuest) {
        undoQuestDB.delete(quest.id!!)
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

