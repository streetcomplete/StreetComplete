package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.ApplicationConstants.MAX_QUEST_UNDO_HISTORY_AGE
import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.OsmQuestGiver
import de.westnordost.streetcomplete.data.osm.download.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.persist.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.statistics.QuestStatisticsDao
import de.westnordost.streetcomplete.data.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.util.SlippyMapMath
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all answered osm quests from local DB and uploads them via the OSM API */
class OsmQuestsUploader @Inject constructor(
    elementDB: MergedElementDao,
    elementGeometryDB: ElementGeometryDao,
    changesetManager: OpenQuestChangesetsManager,
    questGiver: OsmQuestGiver,
    statisticsDB: QuestStatisticsDao,
    osmApiElementGeometryCreator: OsmApiElementGeometryCreator,
    private val questDB: OsmQuestDao,
    private val singleChangeUpload: SingleOsmElementTagChangesUpload,
    private val downloadedTilesDao: DownloadedTilesDao
) : OsmInChangesetsUploader<OsmQuest>(elementDB, elementGeometryDB, changesetManager, questGiver,
    statisticsDB, osmApiElementGeometryCreator) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Applying quest changes")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<OsmQuest> = questDB.getAll(statusIn = listOf(QuestStatus.ANSWERED))

    override fun uploadSingle(changesetId: Long, quest: OsmQuest, element: Element): List<Element> {
        return listOf(singleChangeUpload.upload(changesetId, quest, element))
    }

    override fun onUploadSuccessful(quest: OsmQuest) {
        quest.close()
        questDB.update(quest)
        Log.d(TAG, "Uploaded osm quest ${quest.toLogString()}")
    }

    override fun onUploadFailed(quest: OsmQuest, e: Throwable) {
        /* #812 conflicting quests may not reside in the database, otherwise they would
           wrongfully be candidates for an undo - even though nothing was changed */
        questDB.delete(quest.id!!)
        invalidateAreaAroundQuest(quest)
        Log.d(TAG, "Dropped osm quest ${quest.toLogString()}: ${e.message}")
    }

    private fun invalidateAreaAroundQuest(quest: OsmQuest) {
        // called after a conflict. If there is a conflict, the user is not the only one in that
        // area, so best invalidate all downloaded quests here and redownload on next occasion
        val tile = SlippyMapMath.enclosingTile(quest.center, ApplicationConstants.QUEST_TILE_ZOOM)
        downloadedTilesDao.remove(tile)
    }

    override fun cleanUp(questTypes: Set<OsmElementQuestType<*>>) {
        super.cleanUp(questTypes)
        val timestamp = System.currentTimeMillis() - MAX_QUEST_UNDO_HISTORY_AGE
        questDB.deleteAll(
            statusIn = listOf(QuestStatus.CLOSED, QuestStatus.REVERT),
            changedBefore = timestamp
        )
    }

    companion object {
        private const val TAG = "OsmQuestUpload"
    }
}

private fun OsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId
