package de.westnordost.streetcomplete.data.osm.osmquest

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants.MAX_QUEST_UNDO_HISTORY_AGE
import javax.inject.Inject

import de.westnordost.streetcomplete.data.quest.QuestStatus
import de.westnordost.streetcomplete.data.osm.elementgeometry.OsmApiElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryDao
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osm.upload.changesets.OpenQuestChangesetsManager
import de.westnordost.streetcomplete.data.osm.upload.OsmInChangesetsUploader
import de.westnordost.streetcomplete.util.enclosingTile
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all answered osm quests from local DB and uploads them via the OSM API */
class OsmQuestsUploader @Inject constructor(
        elementDB: MergedElementDao,
        elementGeometryDB: ElementGeometryDao,
        changesetManager: OpenQuestChangesetsManager,
        questGiver: OsmQuestGiver,
        osmApiElementGeometryCreator: OsmApiElementGeometryCreator,
        private val questDB: OsmQuestDao,
        private val singleChangeUploader: SingleOsmElementTagChangesUploader
) : OsmInChangesetsUploader<OsmQuest>(elementDB, elementGeometryDB, changesetManager, questGiver,
    osmApiElementGeometryCreator) {

    @Synchronized override fun upload(cancelled: AtomicBoolean) {
        Log.i(TAG, "Applying quest changes")
        super.upload(cancelled)
    }

    override fun getAll(): Collection<OsmQuest> = questDB.getAll(statusIn = listOf(QuestStatus.ANSWERED))

    override fun uploadSingle(changesetId: Long, quest: OsmQuest, element: Element): List<Element> {
        return listOf(singleChangeUploader.upload(changesetId, quest, element))
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
        Log.d(TAG, "Dropped osm quest ${quest.toLogString()}: ${e.message}")
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
