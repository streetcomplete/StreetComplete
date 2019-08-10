package de.westnordost.streetcomplete.data.osm.upload

import android.util.Log
import de.westnordost.osmapi.map.data.Element
import de.westnordost.streetcomplete.ApplicationConstants.MAX_QUEST_UNDO_HISTORY_AGE
import javax.inject.Inject

import de.westnordost.streetcomplete.data.QuestStatus
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.persist.MergedElementDao
import de.westnordost.streetcomplete.data.osm.persist.OsmQuestDao
import de.westnordost.streetcomplete.data.upload.OnUploadedChangeListener
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/** Gets all answered osm quests from local DB and uploads them via the OSM API */
class OsmQuestsUpload @Inject constructor(
    private val questDB: OsmQuestDao,
    private val elementDB: MergedElementDao,
    private val changesetManager: OpenQuestChangesetsManager,
    private val singleChangeUpload: SingleOsmElementTagChangesUpload
) {
    private val TAG = "OsmQuestUpload"

    var uploadedChangeListener: OnUploadedChangeListener? = null

    @Synchronized fun upload(cancelled: AtomicBoolean) {
        if (cancelled.get()) return
        Log.i(TAG, "Applying quest changes")
        for (quest in questDB.getAll(null, QuestStatus.ANSWERED)) {
            if (cancelled.get()) break

            try {
                uploadSingle(quest)
                quest.status = QuestStatus.CLOSED
                questDB.update(quest)
                Log.d(TAG, "Uploaded osm quest ${quest.toLogString()}")
                uploadedChangeListener?.onUploaded()
            } catch (e: ElementConflictException) {
                Log.d(TAG, "Dropped osm quest ${quest.toLogString()}: ${e.message}")
                /* #812 conflicting quests may not reside in the database, otherwise they would
                   wrongfully be candidates for an undo - even though nothing was changed */
                questDB.delete(quest.id!!)
                uploadedChangeListener?.onDiscarded()
            }
        }

        questDB.deleteAllClosed(System.currentTimeMillis() - MAX_QUEST_UNDO_HISTORY_AGE)
    }

    private fun uploadSingle(quest: OsmQuest) : Element {
        val element = elementDB.get(quest.elementType, quest.elementId)
            ?: throw ElementDeletedException("Element deleted")

        return try {
            val changesetId = changesetManager.getOrCreateChangeset(quest.osmElementQuestType, quest.changesSource!!)
            singleChangeUpload.upload(changesetId, quest, element)
        }  catch (e: ChangesetConflictException) {
            val changesetId = changesetManager.createChangeset(quest.osmElementQuestType, quest.changesSource!!)
            singleChangeUpload.upload(changesetId, quest, element)
        }
    }
}

private fun OsmQuest.toLogString() =
    type.javaClass.simpleName + " for " + elementType.name.toLowerCase(Locale.US) + " #" + elementId
